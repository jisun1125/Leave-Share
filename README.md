# 리브앤쉐어
<img src="https://user-images.githubusercontent.com/55406486/108613548-61a21400-7436-11eb-8c45-016c0689fe49.png"  width="450" height="450">

- 구글 플레이스토어 출시 
* https://play.google.com/store/apps/details?id=kr.ac.kumoh.s20171278.map_map_challenge

## 소개
- 사진을 이용한 손쉬운 여행 기록, 공유 기능을 지원하고 여행 정보 수집을 돕는 모바일 어플리케이션
* 근처 관광지 자동 해시태깅, 관광 정보 조회/검색 기능 추가(한국관광공사 TourAPI 기반)
* 개발 언어: Kotlin
* Android Studio, 한국관광공사 TourAPI 국문 관광정보 서비스, Maps SDK for Android, ExifInterface, Firebase, Cloud Firestore, Cloud Storage for Firebase, Firebase Authentication, FIrebase Dynamic Link

## 어플리케이션 주요 기능
### 이미지의 GPS 좌표, 시간 기반 자동 그룹 분류
* 사용자가 선택한 이미지를 기준에 따라 자동 분류 후 지도, 타임라인 형태로 나타냄
* 그룹별 제목, 메모, 해시태그 입력 가능

![image](https://user-images.githubusercontent.com/55406486/92082954-3bdd7000-ee00-11ea-8c09-f6165e3ee764.png)
![image](https://user-images.githubusercontent.com/55406486/92082986-4861c880-ee00-11ea-9395-4526b98314ee.png)

![image](https://user-images.githubusercontent.com/55406486/92083144-852dbf80-ee00-11ea-99e5-dc49395ebe06.png)
![image](https://user-images.githubusercontent.com/55406486/92083312-d0e06900-ee00-11ea-8d2f-865afcab3b62.png)

(해시태깅이 완료된 캡쳐 추가)

### 근처 관광지 자동 해시태깅
* 분류된 그룹별로 GPS 좌표를 바탕으로 근처 관광지의 해시태그를 자동으로 추가
* 해시태깅을 원하는 그룹을 선택해 추가 가능

(캡쳐 추가)

### 근처 관광지 정보 조회
* GPS 좌표를 기준으로 근처 관광지 정보를 제공함
* 원하는 관광지 목록을 클릭해 상세 정보 조회 가능

<img src="https://user-images.githubusercontent.com/55406486/108613639-2e13b980-7437-11eb-878d-4102f2b162b7.jpg" width="280" >


### 공유 기능
* 저장된 앨범을 링크 형태로 공유
* 원하는 그룹만 선택하여 전송 가능

![image](https://user-images.githubusercontent.com/55406486/92083369-e786c000-ee00-11ea-9162-7ed177880962.png)
![image](https://user-images.githubusercontent.com/55406486/92083380-eb1a4700-ee00-11ea-8ed6-c1e71d72cadd.png)

(공유 링크 도메인 변경)

(mapmapchallenge.page.link -> leaveshare.page.link)

* 공유 받기
* 링크 접속 시 해당 화면으로 이동

![image](https://user-images.githubusercontent.com/55406486/92083482-11d87d80-ee01-11ea-91ee-296fadc20c09.png)
![image](https://user-images.githubusercontent.com/55406486/92083506-1866f500-ee01-11ea-937b-856e951e99fb.png)


### 앨범 검색
* 전체 앨범 데이터 내 검색 결과 출력
* 키워드 입력 또는 해시태그 클릭을 통해 검색 가능
* 검색 결과 클릭 시 해당 앨범으로 이동

![image](https://user-images.githubusercontent.com/55406486/92083758-6c71d980-ee01-11ea-998c-21a5ad9fffe9.png)
![image](https://user-images.githubusercontent.com/55406486/92083763-6e3b9d00-ee01-11ea-8dc4-c99c93ede8f0.png)

- 해시태그를 눌러 검색 지원
<img src="https://user-images.githubusercontent.com/55406486/108613638-2b18c900-7437-11eb-8738-6092206533e9.jpg" width="280" >

### 관광지 정보 검색/조회
* 원하는 관광지의 이름, 주소, 사진, 전화번호, 홈페이지, 개요 등의 정보 조회 가능
<img src="https://user-images.githubusercontent.com/55406486/108613636-281dd880-7437-11eb-9ad1-08f99511ec05.jpg" width="280" >


## 개발팀
* 홍지선(hgsnm@naver.com)
* 홍소희(sh_6863@naver.com)
