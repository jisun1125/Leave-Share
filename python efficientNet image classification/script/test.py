# -*- coding: utf-8 -*-
"""
Created on Wed Aug  5 00:27:27 2020

@author: user
"""
import numpy as np
import json
from PIL import Image
import torch
import torch.nn as nn
import torch.optim as optim
from torch.optim import lr_scheduler
from torchvision import transforms
import matplotlib.pyplot as plt
import time
import os
import copy
import random

from efficientnet_pytorch import EfficientNet
model_name = 'efficientnet-b0'  # b0

image_size = EfficientNet.get_image_size(model_name)  # 224 x 224
# print(image_size)
model = EfficientNet.from_pretrained(model_name, num_classes=11)

## load data
batch_size  = 64
random_seed = 555
random.seed(random_seed)
torch.manual_seed(random_seed)

## make dataset
from torchvision import datasets
data_path = '/content/gdrive/My Drive/Colab Notebooks/data_to_pre'  # class 별 나눠진 폴더에서 데이터를 가져옴
president_dataset = datasets.ImageFolder(
                                data_path,
                                transforms.Compose([
                                    transforms.Resize((224, 224)),
                                    transforms.ToTensor(),
                                    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
                                ]))
## data split
from sklearn.model_selection import train_test_split
from torch.utils.data import Subset
train_idx, tmp_idx = train_test_split(list(range(len(president_dataset))), test_size=0.4, random_state=random_seed)
datasets = {}
datasets['train'] = Subset(president_dataset, train_idx)
tmp_dataset       = Subset(president_dataset, tmp_idx)

val_idx, test_idx = train_test_split(list(range(len(tmp_dataset))), test_size=0.75, random_state=random_seed)
datasets['valid'] = Subset(tmp_dataset, val_idx)
datasets['test']  = Subset(tmp_dataset, test_idx)

## data loader 선언
dataloaders, batch_num = {}, {}
dataloaders['train'] = torch.utils.data.DataLoader(datasets['train'],
                                              batch_size=batch_size, shuffle=True,
                                              num_workers=3)
dataloaders['valid'] = torch.utils.data.DataLoader(datasets['valid'],
                                              batch_size=batch_size, shuffle=False,
                                              num_workers=3)
dataloaders['test']  = torch.utils.data.DataLoader(datasets['test'],
                                              batch_size=batch_size, shuffle=False,
                                              num_workers=3)
batch_num['train'], batch_num['valid'], batch_num['test'] = len(dataloaders['train']), len(dataloaders['valid']), len(dataloaders['test'])
print('batch_size : %d,  train / valid / test : %d / %d / %d' % (batch_size, batch_num['train'], batch_num['valid'], batch_num['test']))

def imshow(inp, title=None):
    """Imshow for Tensor."""
    inp = inp.numpy().transpose((1, 2, 0))
    mean = np.array([0.485, 0.456, 0.406])
    std = np.array([0.229, 0.224, 0.225])
    inp = std * inp + mean
    inp = np.clip(inp, 0, 1)
    plt.imshow(inp)
    if title is not None:
        plt.title(title)
    plt.tight_layout()
    plt.pause(0.001)  # pause a bit so that plots are updated

class_names = {
    "0": "bulgogi",      # "0": "불고기"
    "1": "chicken",   # "1": "치킨"
    "2": "dumpling",   # "2": "만두"
    "3": "forest",  # "3": "숲"
    "4": "palace", #  "4": "고궁"
    "5": "sea",  # "5": "바다"
    "6": "seaweedsoup",  # "6": "미역국"
    "7": "statue",  # "7": "불상"
    "8": "stonetower",  # "8": "석탑"
    "9": "sundae", # "9": "순대"
    "10": "tomb"  # "10": "무덤"
}


def test_and_visualize_model(model, phase = 'test', num_images=10):
    # phase = 'train', 'valid', 'test'
    model.eval()
    fig = plt.figure()
    
    running_loss, running_corrects, num_cnt = 0.0, 0, 0
    criterion = nn.CrossEntropyLoss()

    class_correct = list(0. for i in range(11))
    class_total = list(0. for i in range(11))  # class 개수만큼 list 생성

    with torch.no_grad():
        for i, (inputs, labels) in enumerate(dataloaders[phase]):
            inputs = inputs.to(device)
            labels = labels.to(device)
            outputs = model(inputs)
            _, preds = torch.max(outputs, 1)
            loss = criterion(outputs, labels)  # batch의 평균 loss 출력

            for j in range(len(inputs)):
              class_total[labels[j].cpu().numpy()] += 1
              if class_names[str(labels[j].cpu().numpy())]==class_names[str(preds[j].cpu().numpy())]:
                class_correct[labels[j].cpu().numpy()] += 1
                
            running_loss    += loss.item() * inputs.size(0)
            running_corrects+= torch.sum(preds == labels.data)
            num_cnt += inputs.size(0)  # batch size

        test_loss = running_loss / num_cnt
        test_acc  = running_corrects.double() / num_cnt       
    
    # 결과 출력
    print('Accuracy of the network on the test images: %d %% (%d/%d)' % (running_corrects.double()*100 / num_cnt, running_corrects, num_cnt))
    for i in range(11):
      print('Accurach of %11s : %2d %% (%d/%d)' % (class_names[str(i)], 100 * class_correct[i] / class_total[i], class_correct[i], class_total[i]))

    # 예시 그림 plot
    with torch.no_grad():
        for i, (inputs, labels) in enumerate(dataloaders[phase]):
            inputs = inputs.to(device)
            labels = labels.to(device)

            outputs = model(inputs)
            _, preds = torch.max(outputs, 1)        

            for j in range(1, num_images+1):
                ax = plt.subplot(num_images//2, 2, j)
                plt.figure(figsize=(12,5))
                ax.axis('off')
                if class_names[str(labels[j].cpu().numpy())]==class_names[str(preds[j].cpu().numpy())]:
                  tmptitle = '[True] ' + class_names[str(labels[j].cpu().numpy())] + ' (predicted: ' + class_names[str(preds[j].cpu().numpy())] + ')'
                else:
                  tmptitle = '[False] ' + class_names[str(labels[j].cpu().numpy())] + ' (predicted: ' + class_names[str(preds[j].cpu().numpy())] + ')'
                imshow(inputs.cpu().data[j], tmptitle)          
            if i == 0 : break

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")  # set gpu

model = model.to(device)
state_dict = model.state_dict()

checkpoint = torch.load('/content/gdrive/My Drive/class11_torch_model.pt')
avoid = ['fc.weight', 'fc.bias']
for key in checkpoint.keys():
    if key in avoid or key not in state_dict.keys():
        continue
    if checkpoint[key].size() != state_dict[key].size():
        continue
    state_dict[key] = checkpoint[key]
model.load_state_dict(state_dict)
test_and_visualize_model(model, phase = 'v

