# Android Training(2022.09.17~)

- Clean architecture 적용
- 레이어간 명확한 분리를 위해 멀티모듈로 구성(app, presentation, domain, data)
- DI(Hilt) 적용
- MVVM, MVI패턴 적용
- DataBinding 사용
- AAC Viewmodel사용
- Navigation Component 적용
- Retrofit, Glide 사용    
- RecyclerView 어댑터의 5가지 사용사례 구현   
RecyclerView.Adabper   
DiffUtil - AsyncListDiffer   
ListAdapter   
Paging3 - PagingSource   
Paging3 - RemoteMediator
- Jetpack compose + Paging3 이용한 도서목록 구현
- SharedElementTransition구현 - 리스트행 선택시 이미지 애니메이션하며 상세보기로 이동
- SlidingPaneLayout구현 - 대화면 최적화 마스터/디테일 레이아웃

## 구현내용
### 1. 도서정보
- 도서정보 OpenAPI활용하여 도서검색, 상세보기 구현   
  (Google books, Kakao 책정보, Naver 책정보)
- 키보드 검색버튼 클릭시 API호출하여 리스트 표시
- 로딩중 프로그레스바 표시   
(첫 검색시에는 화면에 프로그레스바 표시, 두번째 페이지 이상 검색시에는 마지막 셀에 프로그레스바 표시)
- 도서제목에 검색어와 일치하는 텍스트를 SpannableText로 색상강조
- 리스트뷰 헤더에 검색 도서 갯수 표시
- 리스트뷰(RecyclerView)에 정가, 판매가, 할인율등 표현(할인율 0%시에는 정가, 할인율 숨기기)
- 할인 판매인경우 정가에 취소선 표시
- Custom scroll listener구현하여 스크롤다운시 끝에 도달하지 않아도 Threshold 적용하여 다음페이지 로드
- 네트워크단절, 서버오류, 기타오류 발생시 스낵바로 에러메세지 표시
- 검색 결과가 없는경우 검색결과없음 이미지 표시
- 도서 선택시 상세보기 화면 이동   

### 2. 지도
- 지도 종류별 화면 구현(카카오, 네이버, 티맵, 구글맵)
- 카테고리별 장소검색(병원, 약국, 주유소)
- 장소검색 결과 BottomSheet에 뛰우고, 지도에 마커 표시
- 현재위치 얻어서 지도에 마커 표시(권한처리등 포함)
- BottomSheet 스크롤시 지도영역이 가려지지 않도록 스크롤정보 활용하여 지도 위치 이동시켜주기
- 각 플랫폼별로 제공하는 장소검색, 길찾기 API활용 

### 기타
- local.properties에 api_key=""나 client_secret 설정 후 BuildConfig로 활용
- release.apk 포함(app/release/app-release.apk)

## 다음 구현해볼것
1. Google Map, Naver Map, Kakao Map, T Map
- POI검색, 길찾기
2. WebRTC로 1:n 화상회의 구현
- 웹소켓과 푸쉬를 이용한 실시간 채팅
- WebRTC data channel을 이용한 파일전송
2. ExoPlayer를 이용한 동영상 스트리밍