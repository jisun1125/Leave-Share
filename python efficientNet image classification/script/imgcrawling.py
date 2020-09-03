import urllib.request
from bs4 import BeautifulSoup

# 접근할 페이지 번호
pageNum = 22

# 저장할 이미지 경로 및 이름 (data폴더에 face0.jpg 형식으로 저장)
imageNum = 440
imageStr = "data_sea/sea"

while pageNum < 45:
    # url = "https://www.shutterstock.com/ko/search/sky?image_type=photo&mreleased=false&page="
    url = "https://www.shutterstock.com/ko/search/sea?image_type=photo&mreleased=false&page="
    #url = "https://www.shutterstock.com/ko/search/forest?image_type=photo&mreleased=false&page="
    url = url + str(pageNum)

    fp = urllib.request.urlopen(url)
    source = fp.read()
    fp.close()

    soup = BeautifulSoup(source, 'html.parser')
    soup = soup.findAll("a", class_="z_h_81637")

    # 이미지 경로를 받아 로컬에 저장한다.
    for i in soup:
        if imageNum == 20*(pageNum):
            break
        imageNum += 1
        imgURL = i.find("img")["src"]
        urllib.request.urlretrieve(imgURL, imageStr + str(imageNum) + ".jpg")
        print(imgURL)
        print(imageNum)

    pageNum += 1
