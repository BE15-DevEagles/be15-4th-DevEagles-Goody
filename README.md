
<p align="center">
<img src="https://d1085v6s0hknp1.cloudfront.net/boards/coinsect_blog/54d35af9-88dc-4d77-a1a6-dfb6777d9ccf_logo-goody-with-text.png" width="400px"/>
</p>

## 🦅 DevEagles 팀원 소개 

<table style="width: 100%; text-align: center;">
  <tr>
    <td align="center"> <a href="https://github.com/gyeongmin03">김경민</a></td>
    <td align="center"> <a href="https://github.com/wishbornDev">김소원</a></td>
    <td align="center"> <a href="https://github.com/hwan1023">김태환</a></td>
    <td align="center"> <a href="https://github.com/64etuor">박양하</a></td>
    <td align="center"> <a href="https://github.com/Jayboo816">부재녕</a></td>
    <td align="center"> <a href="https://github.com/nineeko">이채은</a></td>
  </tr>
  <tr>
    <td align="center"><img src="https://velog.velcdn.com/images/nare20027/post/80fb190b-e2ae-4ce5-9b62-11676bea73ed/image.png" width="150px" height="170px"/></td>
    <td align="center"><img src="https://velog.velcdn.com/images/nare20027/post/8af86ee2-3f5a-4484-a2cd-2b478c437237/image.png" width="150px" height="170px"/></td>
    <td align="center"><img src="https://velog.velcdn.com/images/nare20027/post/decc4615-744e-4631-b686-0dc7418c66a4/image.png" width="150px" height="170px"/></td>
    <td align="center"><img src="https://velog.velcdn.com/images/nare20027/post/1842035b-7a20-4a3a-a6d9-9ff3bff4a2c0/image.png" width="150px" height="170px"/></td>
    <td align="center"><img src="https://velog.velcdn.com/images/nare20027/post/9a40d1a3-966c-41d7-a153-bbfd93c813a1/image.png" width="150px" height="170px"/></td>
    <td align="center"><img src="https://velog.velcdn.com/images/nare20027/post/20d214f1-b01e-4733-89ae-35740d1f0ffc/image.png" width="150px" height="170px"/></td>
  </tr>
</table>

<br><br>


## 💻 Goody 개요

📑 <a href="#1">1. 프로젝트 기획</a>
  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-1">1-1. 주제</a>

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-2">1-2. 배경 및 필요성</a>
 
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-3">1-3. 차별점</a>
 
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-4">1-4. 주요 기능</a>
  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-5">1-5. 요구사항 명세서 </a>

🗃️ <a href="#2">2. DB 모델링</a>

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-1">2-1. 논리 /물리 모델링</a>

  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-2">2-2. DDL </a>
  
📲 <a href="#3">3. 화면 설계</a>

⚙️ <a href="#4">4. 빌드 및 배포 문서 </a>

 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#4-1">4-1. Jenkins Pipeline Script </a>

🛠️ <a href="#5">5. API문서 </a>

 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#5-1">5-1. SwaggerAPI 문서 </a>


🗒️ <a href="#6">6. 프로젝트 아키텍처 구조 </a>

 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#6-1">6-1. 아키텍처 구조 </a>

👾 <a href="#7">7. 기술 스택 </a>

💡 <a href="#8">8.테스트 결과 </a>

 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#8-1">8-1. Jenkins CI/CD 테스트 결과 </a>
 
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#8-2">8-2. FE 테스트 결과 </a>

🐞 <a href="#9">9. 트러블슈팅 </a>

🧑‍💻 <a href="#10">10. 팀원 회고 </a>

<br><br>


## <p id="1"> 📑 1. 프로젝트 기획</p>


### <p id="1-1">1-1. 주제</p>
**AI 기반 스마트 협업 그룹웨어 플랫폼** 
> 단기 프로젝트 팀을 위한 감정 케어 + 일정 관리 + 실시간 협업이 가능한 통합형 협업툴

<br><br>

### <p id="1-2">1-2. 배경 및 필요성</p>
* 기존 협업툴은 **기능이 흩어져 있고 복잡**해서 단기 프로젝트 팀이 빠르게 정착하기 어려움
* 특히 캡스톤, 공모전, 사이드 프로젝트처럼 **단기성·비정기성 팀**의 경우
    * 비용 부담 없는 **무료 툴**,
    * 직관적이고 빠른 **온보딩 경험**,
    * **업무 기록, 일정 관리, 실시간 소통**이 한 곳에 **통합**된 환경이 필요함
* 또한 팀원 간 **의사소통의 단절**이나 **지속적 피로감**이 발생하기 쉬운 만큼,

    * 팀 분위기를 부드럽게 만들고
    * **정서적 연결감을 높여주는 기능**도 중요한 요소로 부상 
* 따라서 단순한 협업 기능을 넘어
	
    👉 **"가볍고 빠르게 쓸 수 있으면서도 사람 사이의 연결을 놓치지 않는" 스마트 협업 플랫폼**이 요구됨
    
<br><br>
   
### <p id="1-3">1-3. 차별점</p>

| 항목          | 기존 협업툴 (Slack, Notion, Jira 등) | 우리 협업툴                            |
| ----------- | ------------------------------ | --------------------------------- |
| **AI 활용**   | 일부 유료 기능에서 제한적으로 제공            | **AI 요약, 맞춤법 검사, 감정 체크** 등 다방면 활용 |
| **업무 일지**   | 단순 텍스트 기록, 수정 가능               | **수정 불가 + AI 요약 + 코멘트 기반 구조**     |
| **감정 관리**   | 감정 상태 분석 기능 없음                 | **매일 기분 체크 → 팀원 감정 상태 시각화**       |
| **채팅 기능**   | 실시간 채팅 중심 (Slack)              | **1:1 및 단체방 자동 생성 + 감정 상태 연동**    |
| **일정 관리**   | 외부 캘린더 연동 필요                   | **내장 캘린더 + 디데이/지연 일정 우선 정렬**      |
| **팀 온보딩**   | 초기 설정 복잡, 여러 툴 연동 필요           | **회원가입 → 팀 생성 → 바로 협업 가능**        |
| **팀 문화 요소** | 없음                             | **점심 메뉴 룰렛 등 가벼운 유대감 기능 탑재**      |
| **타임캡슐 기능** | 제공하지 않음                        | **특정일 작성 메시지 → 설정일 자동 공개**        |
| **비용 부담**   | 주요 기능은 유료 플랜에서만 사용 가능          | **핵심 기능 무료 제공 전제**                |

<br><br>

### <p id="1-4">1-4. 주요 기능</p>
#### 📝 업무 일지 기능  
- 사용자가 작성한 일지를 **AI가 자동 요약**하여 글자 수 제한 내 요약 결과 제공  
- **맞춤법 검사 API** 적용 → 결과를 **모달 내에서 직접 수정** 후 저장  
- 일지에 대해 **코멘트 작성, 수정, 삭제 가능 (CRUD)**  
- 작성된 일지는 **수정 및 삭제 불가**, 기록 중심 구조로 보존  
- 작성자, 날짜, 키워드 기준 **필터링 및 최신순/과거순 정렬** 기능 제공 
#### 💬 실시간 채팅 기능  
- **WebSocket + STOMP** 기반의 1:1 채팅 및 자동 단체 채팅방 생성  
- **매일 아침 기분 체크 질문** → 팀원 목록에 **감정 상태 시각화**  
- 채팅방 내 **읽음 여부 표시**, **프로필 이미지 첨부** 가능  
#### 👥 팀 및 회원 관리 기능  
- **팀 생성, 팀원 초대 및 탈퇴 처리** 가능  
- **회원가입, 로그인, 회원정보 수정, 탈퇴 기능 제공**  
- 회원 탈퇴 시에도 해당 사용자의 작성 콘텐츠는 **데이터 보존**  
#### ⏳ 타임캡슐 기능  
- **입사일, 프로젝트 시작일 등 특정 시점에 작성한 메시지**를  
- 사용자가 설정한 **D-Day 이후에 자동 공개**  
#### 📅 일정 관리 (ToDo 리스트 + 캘린더)  
- **캘린더에 일정 등록**, **D-Day 기반 디데이 리스트 자동 생성**  
- **기한이 지난 일정**, **마감 임박 일정**이 상단에 우선 정렬  
- **일지 미작성자에게 안내 제공** + 일지 작성 페이지로 바로 이동 
#### 🎲 점심 메뉴 룰렛  
- 팀원들과 함께 사용할 수 있는 **랜덤 점심 메뉴 추천 기능**  
- 일상 속 소소한 재미를 더해주는 팀 문화 기능  
#### 🛎️ 알림 기능  
- **새로운 채팅 메시지**, **팀 초대 요청** 등 실시간 알림 제공  
- 사용자는 상황에 맞게 알림을 확인하고 바로 이동 가능  

<br><br>

### <p id="1-5">1-5. 요구사항 명세서</p>
<a href="https://docs.google.com/spreadsheets/d/1U8m1JELnGQVUdPE-0LaRpbx8tudPsHUH3hgkjvbynP4/edit?gid=0#gid=0">요구사항 명세서</a>
![요구사항명세서](https://github.com/user-attachments/assets/1bafb098-7c00-40a2-b86e-22c0848ad2c9)


<br><br>

##  <p id="2"> 🗃️ 2. DB모델링</p>


### <p id="2-1">2-1. 논리 / 물리 모델링</p>
![논리/물리 모델](https://d1085v6s0hknp1.cloudfront.net/boards/coinsect_blog/87ea13dd-35bd-4d05-a32b-5be1d790bf97_9kN4nn8iwXL88rFxx%2520%281%29.png)

### <p id="2-2">2-2. DDL</p>
[DDL](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/wiki/H4.-DDL-Script#ddl_v5_20250522)

<br><br>

## <p id="3"> 📲 3. 화면 설계</p>

<details>
<summary> 메인화면 / 채팅 </summary>
  
<img width="653" alt="main" src="https://github.com/user-attachments/assets/87ec5d51-1283-4c8d-b974-9c6c3823a285" />

</details>

<details>
<summary> 회원 </summary>
  
<img width="613" alt="user_login_modal" src="https://github.com/user-attachments/assets/c37c7f56-b9ed-417f-afc6-4182d3ebd3ec" />
<img width="357" alt="enroll" src="https://github.com/user-attachments/assets/8564eacc-6af7-4f86-aaca-f69a52982f2f" />
<img width="423" alt="mypage" src="https://github.com/user-attachments/assets/fa9ea907-6c70-48bf-a3c2-ef3b6b8a1eb6" />

</details>

<details>
<summary> 팀 </summary>
  
<img width="903" alt="team" src="https://github.com/user-attachments/assets/1b9f5b3b-21fd-4065-b483-2ea1650033b8" />
<img width="508" alt="team_todolist" src="https://github.com/user-attachments/assets/26150c58-12c8-43fc-99f1-ec82f98dc30c" />

</details>

<details>
<summary> 내 캘린더 </summary>
  
<img width="824" alt="todo_calendar" src="https://github.com/user-attachments/assets/c057cd12-8151-413c-af48-29dbcd97c478" />

</details>

<details>
<summary> 일지 작성 </summary>
  
<img width="549" alt="worklog1" src="https://github.com/user-attachments/assets/99417934-9289-4b74-aa40-c74cea35f00c" />
<img width="382" alt="worklog2" src="https://github.com/user-attachments/assets/66f26842-17a0-4fc7-b518-7964a2e5a51c" />
<img width="395" alt="worklog3" src="https://github.com/user-attachments/assets/097dad84-473e-4c1c-a869-b2f72ef840ac" />


</details>

<details>
<summary> 타임캡슐 작성 </summary>
  
<img width="527" alt="timecapsule" src="https://github.com/user-attachments/assets/46629098-0e0d-499a-9421-e13622d2cb75" />

</details>

<details>
<summary> 룰렛 </summary>

<img width="493" alt="roullete" src="https://github.com/user-attachments/assets/dd56d75a-acda-43b8-81bf-c2c8ea20e790" />

</details>

<br><br>

##  <p id="4"> ⚙️ 4. 빌드 및 배포 문서</p>
### <p id="4-1">4-1. Jenkins Pipeline Script </p>
``` jenkins
pipeline {
    agent any

    tools {
        gradle 'gradle'
        jdk 'openJDK17'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('DOCKERHUB_PASSWORD')
        GITHUB_URL = 'https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody'
    }

    stages {
        stage('Preparation') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'docker --version'
                        sh 'node -v'
                        sh 'npm -v'
                    } else {
                        bat 'docker --version'
                        bat 'node -v'
                        bat 'npm -v'
                    }
                }
            }
        }

        stage('Checkout Source') {
            steps {
                git branch: 'main', url: "${env.GITHUB_URL}"
            }
        }

        stage('Backend Build') {
            steps {
                dir('be15_DevEagles_BE') {
                    script {
                        if (isUnix()) {
                            sh "chmod +x ./gradlew"
                            sh "./gradlew clean build"
                        } else {
                            bat "gradlew.bat clean build"
                        }
                    }
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('be15_DevEagles_FE') {
                    script {
                        if (isUnix()) {
                            sh "npm install"
                            sh "npm run build"
                        } else {
                            bat "npm install"
                            bat "npm run build"
                        }
                    }
                }
            }
        }

        stage('Docker Build and Push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_PASSWORD', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        // Backend 이미지 빌드 및 푸시
                        dir('be15_DevEagles_BE') {
                            def backendImage = "${DOCKER_USER}/dev-eagles-backend:latest"
                            if (isUnix()) {
                                sh "docker build -t ${backendImage} ."
                                sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}"
                                sh "docker push ${backendImage}"
                            } else {
                                bat "docker build -t ${backendImage} ."
                                bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                                bat "docker push ${backendImage}"
                            }
                        }

                        // Frontend 이미지 빌드 및 푸시
                        dir('be15_DevEagles_FE') {
                            def frontendImage = "${DOCKER_USER}/dev-eagles-frontend:latest"
                            if (isUnix()) {
                                sh "docker build -t ${frontendImage} ."
                                sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}"
                                sh "docker push ${frontendImage}"
                            } else {
                                bat "docker build -t ${frontendImage} ."
                                bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                                bat "docker push ${frontendImage}"
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                if (isUnix()) {
                    sh 'docker logout'
                } else {
                    bat 'docker logout'
                }
            }
        }
         success {
            withCredentials([string(credentialsId: 'discord', variable: 'DISCORD')]) {
                discordSend(
                    description: """
                    **빌드 성공!** :tada:
                    
                    **제목**: ${currentBuild.displayName}
                    **결과**: :white_check_mark: ${currentBuild.currentResult}
                    **실행 시간**: ${currentBuild.duration / 1000}s
                    **링크**: [빌드 결과 보기](${env.BUILD_URL})
                    """,
                    title: "${env.JOB_NAME} 빌드 성공!", 
                    webhookURL: "$DISCORD"
                )
            }
        }
        failure {
            withCredentials([string(credentialsId: 'discord', variable: 'DISCORD')]) {
                discordSend(
                    description: """
                    **빌드 실패!** :x:
                    
                    **제목**: ${currentBuild.displayName}
                    **결과**: :x: ${currentBuild.currentResult}
                    **실행 시간**: ${currentBuild.duration / 1000}s
                    **링크**: [빌드 결과 보기](${env.BUILD_URL})
                    """,
                    title: "${env.JOB_NAME} 빌드 실패!", 
                    webhookURL: "$DISCORD"
                )
            }
        }
    }
}

```

##  <p id="5"> 🛠️ 5. API 문서</p>
### <p id="5-1">5-1. Swagger API 문서</p>
[SWAGGER](https://64etuor.github.io/700_studies/2501-2507-%ED%95%9C%ED%99%94beyond-sw%EC%BA%A0%ED%94%84/1_%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8/4%EC%B0%A8-CICD/api-docs/dist/)

<details>
<summary> 🔍 Swagger API 열기 </summary>
  
![swaggerAPI](https://github.com/user-attachments/assets/b0ad5846-8910-4e24-afcf-6b0d81677ccf)

</details>

<br><br>


## <p id="6"> 🗒️ 6. 프로젝트 아키텍처 구조 </p>
### <p id="6-1">6-1. 아키텍처 구조 </p>
![프로젝트 아키텍처](https://velog.velcdn.com/images/nare20027/post/dda46928-d668-40cd-bfae-320b5c6c8f21/image.png)

<br><br>


## <p id="7"> 👾 7. 기술 스택 </p>

<div dir="auto">
  
####  <p align="center"> Backend & Database </p>
<p align="center">
<img src="https://img.shields.io/badge/JAVA-094394?style=for-the-badge&logo=JAVA&logoColor=white">
<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
<img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
</p>
<p align="center">
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">
<img src="https://img.shields.io/badge/mariaDB-003545?style=for-the-badge&logo=mariaDB&logoColor=white">
<img src="https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=&logoColor=white">
<img src="https://img.shields.io/badge/Mybatis-000000?style=for-the-badge&logo=&logoColor=white">
<img src="https://img.shields.io/badge/mongodb-47A248?style=for-the-badge&logo=mongodb&logoColor=white">
<img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">
</p>

#### <p align="center"> Frontend </p>
<p align="center">
<img src="https://img.shields.io/badge/vue.js-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white">
<img src="https://img.shields.io/badge/axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white">
<img src="https://img.shields.io/badge/vite-646CFF?style=for-the-badge&logo=vite&logoColor=white">
<img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=white">
<img src="https://img.shields.io/badge/tailwindcss-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white">
<img src="https://img.shields.io/badge/pinia-4FC08D?style=for-the-badge&logo=&logoColor=white">
</p>

#### <p align="center"> External API </p>
<p align="center">
<img src="https://img.shields.io/badge/amazons3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">
<img src="https://img.shields.io/badge/smtp-44e393?style=for-the-badge&logo=&logoColor=white">
<img src="https://img.shields.io/badge/googlegemini-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white">
<img src="https://img.shields.io/badge/huggingface-FFD21E?style=for-the-badge&logo=huggingface&logoColor=white">
<img src="https://img.shields.io/badge/FullCalendar-2ba5ec?style=for-the-badge&logo=&logoColor=white">
</p>

#### <p align="center"> Tools & Communication </p>
<p align="center">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
<img src="https://img.shields.io/badge/jira-0052CC?style=for-the-badge&logo=jira&logoColor=white">
<img src="https://img.shields.io/badge/postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white">
<img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black">
<img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white">
<img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white">
<img src="https://img.shields.io/badge/junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white">
</p>

#### <p align="center"> CI / CD </p>
<p align="center">
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
<img src="https://img.shields.io/badge/prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white">
<img src="https://img.shields.io/badge/grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white">
<img src="https://img.shields.io/badge/elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white">
<img src="https://img.shields.io/badge/jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white">
<img src="https://img.shields.io/badge/kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white">
</p>

</div>

<br><br>


## <p id="8"> 💡 8. 테스트 결과 </p>

### <p id="8-1">8-1. Jenkins CI/CD 테스트 결과 </p>

### <p id="8-2">8-2. FE 테스트 결과 </p>

<details>
<summary> 회원 </summary>
  
- 회원가입
  ![user_enroll](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_enroll.gif)
- 로그인
  ![user_login](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_login.gif)
- 마이페이지(회원 정보 수정)
  ![user_mypage](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_mypage.gif)
- 마이페이지(비밀번호 변경)
  ![user_passwordchange](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_pwd_change.gif)
- 로그아웃 
  ![user_logout](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_logout.gif)
- 아이디 찾기
  ![user_findid](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_findid.gif)
- 비밀번호 찾기
  ![user_findpwd](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_findpwd.gif)
- 회원탈퇴
  ![user_withdraw](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_user_withdraw.gif)
  
</details>

<details>
<summary> 팀 </summary>

- 팀원 초대
  ![team_invite](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_team_invite.gif)
- 팀 썸네일 변경
  ![team_thumbnail](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_team_thumbnail.gif)
- 팀장 권한 양도 및 팀 탈퇴
  ![team_withdraw](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_team_withdraw.gif)
- 팀 삭제
  ![team_delete](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_team_delete.gif)
- 팀원 추방 
  ![team_fire](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_team_fire.gif)

</details>

<details>
<summary> 업무 일지 </summary>

- 업무일지 작성 및 ai 요약
  ![worklog_create](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_worklog_create.gif)
- 업무일지 맞춤법 검사
  ![worklog_spell_check](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_worklog_spell.gif)
- 업무일지 상세보기 및 댓글
  ![worklog_select](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_worklog_select.gif)


</details>

<details>
<summary> ToDo </summary>
  
- 내 캘린더 조회
  <br>
  ![todo_mycalendar](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_mycalendar.gif)
- todo 작성
  <br>
  ![todo_create](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_create.gif)
- todo 수정
  <br>
  ![todo_edit](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_edit.gif)
- todo 상세 조회
  <br>
  ![todo_detail](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_selectdetail.gif)
- todo 완료 처리
  <br>
  ![todo_complete](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_complete.gif)
- todo 삭제
  <br>
  ![todo_delete](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_delete.gif)
- 업무일지 작성 여부 조회
  <br>
  ![todo_worklogcheck](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_worklogcheck.gif)
- 팀 캘린더 조회
  <br>
  ![todo_teamcalendar](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_teamcalendar.gif)
- 팀 todolist 목록 필터링 조회
  <br>
  ![todo_filtering](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_todo_filtering.gif)

</details>

<details>
<summary> 채팅 </summary>

- 감정 분석
  ![chat_mind](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_chat_mind.gif)  
- 채팅방 메세지 전송 및 알람 
  ![chat_alam](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_chat_alam.gif)
  
</details>

<details>
<summary> 타임캡슐 </summary>
  
- 타임캡슐 생성
  ![timecapsule_create](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_timecapsule_create.gif)
- 타임캡슐 조회
  ![timecapsule_select](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_timecapule_select.gif)
- 타임캡슐 출력
  ![timecapsule_open](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_timecapsule_open.gif)

</details>

<details>
<summary> 룰렛 </summary>
  
- 룰렛 실행 및 결과 전송
  ![roulette](https://github.com/BE15-DevEagles/be15-4th-DevEagles-Goody/blob/main/be15_DevEagles_FE/src/assets/fe_test/goody_chat_roulette.gif)

</details>

<br><br>


##  <p id="9"> 🐞 9. 트러블슈팅</p>

### 9-1. 특정 `.vue` 파일이 커밋되지 않고 pre-commit에서 실패함 (`defineEmits` 구문 오류)
#### 1️⃣ 문제 코드
```ts
const emit = defineEmits<{
  (e: 'submit', file: File): void;
  (e: 'close'): void;
}>();
```
#### 2️⃣ 서버 에러 코드
```
eslint --fix:
Parsing error: Unexpected token )
```

#### 3️⃣ 발생 원인 
- ##### `defineEmits<T>()` 타입 추론 문법을 `eslint` 혹은 `lint-staged` 플러그인에서 제대로 파싱하지 못함.  또는 Gradle이 `.ts` 확장자를 가진 파일을 Java 코드로 오인해 해석하려는 현상도 의심됨. 즉, 타입스크립트의 최신 문법 또는 Vue 3 setup 문법이 일부 분석 도구와 충돌.


#### 4️⃣ 해결 방법 
##### 일단 해당 타입 선언을 자바스크립트 형태로 간단히 변경하여 우회:
```ts
const emit = defineEmits(['submit', 'close']);
```
##### 근본적인 해결을 위해 아래 항목을 점검하거나 업데이트:
  - `eslint` 버전 확인 (v8 이상 추천)
  - `@typescript-eslint/parser`, `@typescript-eslint/eslint-plugin` 설치/업데이트
  - `.eslintrc` 또는 `eslint.config.js`에 `parserOptions.project` 및 `plugins` 확인
  - `lint-staged`에서 ts 구문을 지원하는 설정인지 확인
  - Gradle이 `.ts` 파일을 건드리지 않도록 exclude 처리

> 🔍 실제 코드상 오류가 아니며, 툴링 해석 문제로 커밋이 막힌 경우. 빠르게 우회하고자 하면 타입 제거 후 나중에 되돌리는 방법도 고려 가능.

<br>

### 9-2. 룰렛 바늘이 가리키는 슬라이스와 결과값이 일치하지 않음
#### 1️⃣ 문제 코드
```js
function getResultIndex(n, angle) {
  const sliceAngle = 360 / n;
  let pointer = (angle - 270 + 360) % 360;
  let idx = Math.floor(pointer / sliceAngle) % n;
  return idx;
}
```
#### 2️⃣ 서버 에러 코드


#### 3️⃣ 발생 원인 
- ##### SVG 회전 각도 기준(0도=오른쪽)과 바늘 위치(270도=위쪽)가 다름  
- ##### `slice` 경계선, 텍스트, path 중심 등의 기준점이 일관되지 않아, 특히 2개일 때 바늘이 항상 경계선에 걸림  
- ##### 바늘은 실제로 `slice`의 "중앙"을 가리키는데, 계산은 `slice`의 "시작점" 기준으로 되어 있었음


#### 4️⃣ 해결 방법 
- ##### 모든 각도 기준에 `sliceAngle / 2` 보정을 추가하여 바늘이 slice 중심을 정확히 가리키도록 통일
```ts
const emit = defineEmits(['submit', 'close']);
```
- ##### `getResultIndex()` 함수 보정:
```js
function getResultIndex(n, angle) {
  const sliceAngle = 360 / n;
  let pointer = (angle - 270 + sliceAngle / 2 + 360) % 360;
  let idx = Math.floor(pointer / sliceAngle) % n;
  return idx;
}
```

<br>

### 9-3. 프로필 이미지 제거 후 저장이 안됨
#### 1️⃣ 문제 코드
```js
if (user.profileImage instanceof File) {
  formData.append('profile', user.profileImage);
}
```

#### 2️⃣ 서버 에러 코드
#### 3️⃣ 발생 원인
- ##### 이미지를 제거했을 경우 `profileImage`는 `null`이지만 아무것도 보내지 않으면 서버는 기존 이미지를 유지하려 함.

#### 4️⃣ 해결 방법
- ##### 비어있는 값을 서버에 전달해 profile != null && profile.isEmpty() 로 만든다. 
```js
if (user.profileImage instanceof File) {
  formData.append('profile', user.profileImage);
} else {
  formData.append('profile', new Blob([], { type: 'application/octet-stream' }), '');
}
```

<br>

### 9-4. JSON과 파일을 함께 formData로 전송 시 400 에러
#### 1️⃣ 문제 코드
```js
formData.append('request', requestPayload); // 오류 발생
```
#### 2️⃣ 서버 에러 코드
400 Bad Request

#### 3️⃣ 발생 원인
- ##### `requestPayload`는 객체인데 Blob으로 감싸주지 않으면 전송 포맷 오류

#### 4️⃣ 해결 방법
```js
formData.append(
  'request',
  new Blob([JSON.stringify(requestPayload)], {
    type: 'application/json',
  })
);
```

<br>

### 9-5. pre-commit hook에서 eslint 오류 발생

#### 1️⃣ 문제 코드
```bash
✖ 1 problem (1 error, 0 warnings)
❌ [Error] lint-staged failed
husky - pre-commit script failed (code 1)
```

#### 2️⃣ 서버 에러 코드

#### 3️⃣ 발생 원인
- ##### 불필요한 escape 문자 `\[` `\"` 등으로 lint 실패
#### 4️⃣ 해결 방법
- ##### 코드에서 불필요한 이스케이프 제거  
``` javascript 
`"\["` → `"["`
```

<br>

### 9-6. 프론트에서 저장한 시간과 DB 저장 시간 불일치
#### 1️⃣ 문제 코드
```vue
<!-- 프론트에서 선택한 시간 -->
<input type="datetime-local" v-model="selectedDateTime" />
```

#### 2️⃣ 서버 에러 코드

#### 3️⃣ 발생 원인
- ##### Spring Boot 기본 시간대는 `UTC`, Vue는 `KST`(한국 표준시) 기준
- ##### 프론트에서 선택한 시간은 한국 시간 기준인데, 서버에서는 UTC 기준으로 저장됨 → 9시간 차이 발생

#### 4️⃣ 해결 방법
- ##### 서버에서 시간대 일관성 유지:
```java
// application.yml
spring:
  jackson:
    time-zone: Asia/Seoul
```
- ##### 프론트에서는 KST 기준으로 시간을 보여주며 실제 문제는 없어, 국제화를 고려한 설계로 정의

> 🔍 DB 저장 시간은 UTC지만 프론트와의 상호작용은 KST 기준으로 일치되도록 처리하는 방식도 충분히 유효함

<br>

### 9-7. V-Calendar 라이브러리에서 VDatePicker 불러오기 오류
#### 1️⃣ 문제 코드
```js
import { VDatePicker } from 'v-calendar'; // 오류 발생
```

#### 2️⃣ 서버 에러 코드
```
[plugin:vite:import-analysis] Failed to resolve import "v-calendar"
```

#### 3️⃣ 발생 원인
- ##### `@vuepic/vue-datepicker`와 `v-calendar`를 혼용하거나, `v-calendar`에서 지원하지 않는 방식으로 컴포넌트를 import
- ##### 또는 `v-calendar`의 버전별로 export 방식이 다름 (특히 setupCalendar 사용 여부)

#### 4️⃣ 해결 방법
##### 1. `v-calendar` 문서에 따라 올바르게 설정:
```ts
import { setupCalendar, DatePicker, Calendar } from 'v-calendar';
setupCalendar(app);
```

##### 2. 컴포넌트 등록 예시:
```vue
<template>
  <DatePicker v-model="date" />
</template>
```

##### 3. 혼용 라이브러리는 제거하고 하나로 통일

> 🔍 `v-calendar`를 사용할 땐 setup 방식과 컴포넌트 이름이 라이브러리 버전에 따라 달라질 수 있으므로 공식 문서를 먼저 확인하는 게 안전함

<br><br>


##  <p id="10"> 🧑‍💻 10. 팀원 회고</p>
| 이름 | 내용 |
| --- | --- |
| 김경민 | 이번 프로젝트에서는 짧은 개발 기간으로 인해 간단한 기능을 구현했고, Docker와 Kubernetes, Jenkins를 다뤄볼 수 있었다. 간단한 기능이라 개발에서는 크게 막히는 부분은 없었지만 CI/CD 환경을 구축하는 부분에서 엄청난 어려움을 겪었다. 비교적 Docker의 Image와 Container를 다루는건 어렵지 않았지만, Kubernetes를 이용하여 환경을 구축할 때 정말 큰 벽을 마주한 것 같았다. 엄청난 시간과 노력을 쏟아 문제를 해결하려고 했지만 계속해서 벽에 막혀 정말 힘든 시간을 보냈다. 하지만 계속해서 문제를 해결하기 위해 노력했더니 결국 문제를 해결하고 환경 구축에 성공했다. 이번 프로젝트를 통해서 기술적으로 성장한 부분은 많지 않지만 정말 귀중한 경험을 얻을 수 있었다. 끝까지 할 수 있다고 응원을 해준 팀원들에게 정말 고마움을 느끼고 최종 프로젝트를 위해 더 열심히 달려야겠다. |
| 김소원 | 이번 프로젝트에서는 유저 도메인의 백엔드와 프론트엔드를 함께 맡아 회원가입, 로그인, 로그아웃, 프로필 수정 기능을 구현했다. 처음으로 Spring Security와 JWT를 직접 적용하면서 인증과 인가의 개념을 코드로 이해하게 되었고, 필터 체인을 따라가며 토큰 검증이 어떻게 이루어지는지, 인증 정보는 어디에 저장되는지를 하나씩 파악해 나갔다. 구현 도중에는 인가되지 않은 요청에 대한 예외 처리, 인증 실패 시 응답 포맷 맞추기 등 세세한 부분에서도 많은 고민이 필요했다. 프론트에서는 로그인 이후 상태 유지와 로그아웃 처리, 프로필 이미지 업로드 및 삭제까지 사용자 경험 흐름을 고려한 구현에 집중했다. 그 과정에서 인증 구조에 대한 감을 잡았고, 복잡한 흐름 속에서 어떤 로직이 필요한지 판단하고 설계하는 힘을 기를 수 있었다. Redis를 활용해 리프레시 토큰을 저장하는 구조를 처음 적용해보며 캐시 시스템에 대한 기초적인 이해를 넓혔고, Jira를 통해 팀 내 이슈를 관리하고 업무 흐름을 시각화하면서 협업 도구의 중요성도 크게 느낄 수 있었다. CI/CD 구축 과정은 직접 담당하진 않았지만, 팀 내에서 이루어지는 Jenkins와 Docker, Kubernetes 기반의 배포 환경을 함께 살펴보며 실무적인 배포 구조와 자동화 흐름에 대한 이해도를 높일 수 있었다. 이슈를 해결해 나가는 과정에서, 서로의 맥락을 이해하고 맞춰가는 일이 생각보다 더 중요하다는 걸 느꼈다. 혼자서 해결할 수 있는 일보다 함께 조율해야 할 일이 더 많았고, 그 과정 자체가 협업이라는 걸 체감했다. 아쉬웠던 부분은, 내가 직접 다루지 않은 영역에 대해 충분히 질문하거나 깊이 파고들지 못했던 때가 있었다. 앞으로는 기능 구현을 넘어서, 흐름과 구조를 함께 고민할 수 있는 개발자가 되고 싶다. |
| 김태환 | 이번 개발을 통해 프론트엔드와 백엔드 개발 역량을 보다 키울 수 있었습니다. 짧은 시간안에 기능을 완성 해야 했지만, 팀원들과 분업하고 소통하며 완성도 높은 결과를 만들 수 있었습니다. 이번 프로젝트에서 저의 역할은 Vue 3와 Spring Boot 기반의 업무일지 관리 서비스, 검색, 정렬, 댓글, AI 요약, 맞춤법 검사 기능을 담당했습니다. 개발 과정에서 발생한 UI 겹침, 외부 API 예외, 테스트 실패 등의 문제를 직접 해결하며 디버깅 역량과 문제 해결 능력을 키울 수 있었습니다. 프론트와 백엔드의 긴밀한 연동, 팀원 간 협업을 통해 실무 감각을 익히고, 전체 서비스의 품질과 사용자 경험을 높이기 위한 고민과 개선을 반복하며 개발자로서 한 단계 성장할 수 있는 기회였습니다.|
| 박양하 | 채팅에 감정 분석 API, OpenAI API 등을 연동해 볼 수 있어서 개인적으로는 재밌는 프로젝트였다. 우리가 일반적으로 생각하는 일반적인 채팅과 연관된 여러 가지 기능들, 상태 관리, 읽음 처리, 알람 설정 등을 시도해봤는데 이게 사용자의 입장으로 쓸 땐 몰랐는데 막상 직접 구현하려니 생각보다 훨씬 복잡하고 어렵게 다가왔다. 바로 이전 프로젝트에서 잘못된 데이터베이스 모델을 가지고 채팅 부가 기능을 구현하다가 여러 난항에 부딪혔던 경험을 살려서 데이터베이스 설계에 많이 집중했다. 경험이 부족하니 인터넷에서 얻은 기술 자료를 많이 참고했고 강사님께도 피드백을 받았다. 지금 뒤돌아 보면 그나마 여러가지 검증을 거쳐서 데이터베이스 모델링을 한 덕분에 그나마 기능을 완성이라도 했지 싶다. 데이터베이스 설계의 중요성을 한 번 더 뼈저리게 실감한다. 이번 프로젝트에는 Jira로 백로그, 이슈 관리를 했는데 다른 팀원들은 정해진 이슈를 모두 기한 내에 끝냈지만 난 끝내 마무리하지 못한 몇 가지 기능들이 있어서 너무 아쉽다.  특히 프로젝트 후반에 시간이 부족해서 카프카를 못 붙인 게 아쉽고 좀 더 체계적인 일정 관리가 필요하다는 생각이다. 못 다한 기능은 어쨌든 개인적으로라도 더 구현해 볼 생각이고, 이번 프로젝트의 내 실책을 다음 프로젝트에서 만회할 기회가 있었으면 좋겠다. 짧은 시간동안 진행된 프로젝트인데 주어진 역할에 최선을 다하는 팀원들에게 좋은 영향을 받게 된 시간이었다. 최종 프로젝트 전에 잠깐 합을 맞춰본 것치고는 정말 괜찮은 결과물이 나왔다고 생각하고, 그래서 더더욱 최종 프로젝트가 기대된다. 다들 정말 고생 많이 하셨습니다! 🙂|
| 부재녕 | 이번 프로젝트를 통해 정말 많은 것을 배우고 성장할 수 있었습니다. 처음에는 이렇게 복잡할 줄 몰랐던 기능들을 하나씩 완성해나가면서 정말 큰 성취감을 느꼈다. 특히 룰렛이 정확하게 동작하는 순간, 타임캡슐 애니메이션이 완벽하게 구현된 순간의 그 느낌은 잊을 수 없었다. "바늘이 가리키는 곳이 결과가 되어야 한다"는 당연해 보이는 요구사항을 만족시키기 위해 SVG 좌표계, 각도 계산, 애니메이션 타이밍까지 모든 것을 다시 설계해야 했던 경험은 디테일의 중요성을 느끼게 해주었다. 처음에는 각도 계산이 계속 틀려서 정말 답답했는데, sliceAngle/2 보정을 통해 문제를 해결했을 때의 그 기분은 잊을 수 없다. 이런 경험을 통해 논리적 사고력과 문제 해결 능력이 한층 더 향상된 것 같다. 또한, 혼자서는 절대 해결할 수 없었던 문제들을 팀원들과 함께 해결해나가는 과정이 정말 값진 경험이었다. 문제를 명확하게 설명하고, 구체적인 피드백을 주고받으며, 서로의 아이디어를 발전시켜나가는 과정에서 협업의 진정한 가치를 느꼈다. 이번 프로젝트는 나에게 단순한 기능 구현을 넘어서 개발자로서의 성장을 가져다준 값진 경험이었다. 기술적 역량 향상은 물론이고, 사용자 중심 사고, 문제 해결 능력, 협업 스킬까지 모든 면에서 한 단계 성장할 수 있었다. 정말 뜻깊고 재미있는 개발 여정이었다.  |
| 이채은 | 이번 프로젝트에서는 풀캘린더, 로깅(ELK), 모니터링(Prometheus + Grafana) 같은 기술들을 실무 수준에서 직접 다뤄보면서, 단순히 구현을 넘어서서 서비스 운영에 필요한 요소들까지 생각할 수 있게 됐다. 풀캘린더를 처음 사용할 때는 단순히 달력에 일정만 띄우면 될 줄 알았다. 근데 실제로 구현하다 보니까 일정 필터링, 툴팁 처리, 모달 연결, 그리고 팀원별 색상 구분까지 할 게 정말 많았다. 사용자 UX를 고려한 섬세한 구현이 필요하다는 것을 느꼈다. 로깅은 개발자만 보는 게 아니라, 운영을 위한 데이터 수집 수단이라는 점에서 구조적 로그와 환경 분리가 중요하다는 것을 느꼈고 모니터링 시스템은 "보여주는 것"이 아니라, "신뢰할 수 있는 서비스"를 만드는 기반이라는 점을 깨달았다. |

