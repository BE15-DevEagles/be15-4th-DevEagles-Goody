<template>
  <div
    class="min-h-screen bg-gradient-to-br from-[var(--color-primary-50)] via-[var(--color-secondary-50)] to-[var(--color-primary-100)] relative overflow-hidden"
  >
    <!-- 배경 장식 요소들 -->
    <div class="absolute inset-0 overflow-hidden">
      <div
        class="absolute -top-40 -right-40 w-80 h-80 bg-gradient-to-br from-[var(--color-primary-200)] to-[var(--color-secondary-200)] rounded-full opacity-20 animate-pulse"
      ></div>
      <div
        class="absolute -bottom-40 -left-40 w-96 h-96 bg-gradient-to-br from-[var(--color-primary-300)] to-[var(--color-secondary-300)] rounded-full opacity-15 animate-pulse"
        style="animation-delay: 1s"
      ></div>
      <div
        class="absolute top-1/4 left-1/4 w-32 h-32 bg-gradient-to-br from-[var(--color-secondary-200)] to-[var(--color-secondary-300)] rounded-full opacity-25 animate-bounce"
        style="animation-delay: 2s"
      ></div>
      <div
        class="absolute top-3/4 right-1/4 w-24 h-24 bg-gradient-to-br from-[var(--color-primary-200)] to-[var(--color-primary-300)] rounded-full opacity-20 animate-bounce"
        style="animation-delay: 3s"
      ></div>
    </div>

    <!-- 메인 히어로 섹션 -->
    <div class="relative z-10">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-16 md:py-24">
        <div class="text-center">
          <!-- 로고 섹션 -->
          <div class="mb-12 flex justify-center">
            <div class="relative group">
              <div
                class="absolute -inset-4 bg-gradient-to-r from-[var(--color-primary-300)] via-[var(--color-secondary-300)] to-[var(--color-primary-400)] rounded-full opacity-30 group-hover:opacity-50 transition-opacity duration-300 animate-pulse"
              ></div>
              <div
                class="relative w-32 h-32 bg-white/90 backdrop-blur-sm rounded-full shadow-2xl flex items-center justify-center p-4 group-hover:scale-105 transition-transform duration-300"
              >
                <img
                  src="/assets/image/logo-goody-with-text.png"
                  alt="Goody Logo"
                  class="w-full h-full object-contain"
                />
              </div>
            </div>
          </div>

          <!-- 메인 타이틀 -->
          <div class="mb-8">
            <h1
              class="text-6xl md:text-8xl font-black bg-gradient-to-r from-[var(--color-primary-main)] via-[var(--color-primary-400)] to-[var(--color-secondary-main)] bg-clip-text text-transparent mb-4"
            >
              Goody
            </h1>
            <div class="flex justify-center items-center space-x-2 mb-6">
              <div
                class="w-12 h-1 bg-gradient-to-r from-[var(--color-primary-300)] to-[var(--color-secondary-300)] rounded-full"
              ></div>
              <div
                class="w-3 h-3 bg-gradient-to-r from-[var(--color-primary-main)] to-[var(--color-secondary-main)] rounded-full animate-ping"
              ></div>
              <div
                class="w-12 h-1 bg-gradient-to-r from-[var(--color-secondary-300)] to-[var(--color-primary-300)] rounded-full"
              ></div>
            </div>
          </div>

          <!-- 서브 타이틀 -->
          <p
            class="text-2xl md:text-3xl font-semibold text-[var(--color-gray-800)] mb-4 max-w-4xl mx-auto"
          >
            <span
              class="bg-gradient-to-r from-[var(--color-primary-main)] to-[var(--color-secondary-main)] bg-clip-text text-transparent"
              >협업의 새로운 기준</span
            >,
            <span class="text-[var(--color-gray-700)]">Goody</span>
          </p>
          <p
            class="text-lg md:text-xl text-[var(--color-gray-600)] mb-12 max-w-3xl mx-auto leading-relaxed"
          >
            팀 프로젝트, 일정 공유, 업무 관리를 위한
            <span class="font-semibold text-[var(--color-primary-main)]">워크스페이스</span>입니다.
            지금 바로 시작하여 생산성을 높여보세요.
          </p>

          <!-- CTA 버튼들 -->
          <div
            class="flex flex-col sm:flex-row justify-center items-center space-y-4 sm:space-y-0 sm:space-x-8"
          >
            <BaseButton
              type="primary"
              class="group relative px-12 py-5 text-lg font-bold shadow-2xl hover:shadow-[var(--color-primary-300)]/25 transition-all duration-300 rounded-2xl bg-gradient-to-r from-[var(--color-primary-main)] to-[var(--color-primary-400)] hover:from-[var(--color-primary-400)] hover:to-[var(--color-primary-500)] transform hover:scale-105"
              @click="$router.push('/calendar/team')"
            >
              <span class="relative z-10">팀 캘린더 시작하기</span>
              <div
                class="absolute inset-0 bg-white/20 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300"
              ></div>
            </BaseButton>
            <BaseButton
              type="secondary"
              class="group px-12 py-5 text-lg font-bold rounded-2xl border-2 border-[var(--color-gray-300)] hover:border-[var(--color-secondary-main)] bg-white/80 backdrop-blur-sm hover:bg-white transition-all duration-300 transform hover:scale-105"
              @click="$router.push('/todos')"
            >
              <span
                class="bg-gradient-to-r from-[var(--color-gray-700)] to-[var(--color-gray-900)] bg-clip-text text-transparent group-hover:from-[var(--color-primary-main)] group-hover:to-[var(--color-secondary-main)]"
                >할 일 확인하기</span
              >
            </BaseButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 기능 소개 섹션 -->
    <div class="bg-white/80 backdrop-blur-sm">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-16 md:py-24">
        <div class="text-center mb-16">
          <h2
            class="text-4xl md:text-5xl font-bold bg-gradient-to-r from-[var(--color-gray-800)] to-[var(--color-gray-900)] bg-clip-text text-transparent mb-5"
          >
            Goody의 핵심 기능
          </h2>
          <p class="text-lg md:text-xl text-[var(--color-gray-600)] max-w-xl mx-auto">
            팀 협업과 생산성 향상을 위한 모든 도구를 한 곳에서 만나보세요.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <!-- 팀 캘린더 -->
          <div
            class="group bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl border border-[var(--color-primary-100)] p-8 hover:shadow-2xl hover:border-[var(--color-primary-300)] transition-all duration-300 cursor-pointer transform hover:-translate-y-2"
            @click="$router.push('/calendar/team')"
          >
            <div
              class="w-16 h-16 bg-gradient-to-br from-[var(--color-primary-main)] to-[var(--color-primary-400)] rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300"
            >
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
            <h3 class="text-2xl font-semibold text-[var(--color-gray-900)] mb-3">팀 캘린더</h3>
            <p class="text-[var(--color-gray-600)] leading-relaxed">
              팀 일정을 공유하고 중요한 이벤트를 한눈에 파악하여 효율적으로 협업하세요.
            </p>
          </div>

          <!-- 업무일지 -->
          <div
            class="group bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl border border-[var(--color-success-100)] p-8 hover:shadow-2xl hover:border-[var(--color-success-300)] transition-all duration-300 cursor-pointer transform hover:-translate-y-2"
            @click="$router.push('/worklog/my')"
          >
            <div
              class="w-16 h-16 bg-gradient-to-br from-[var(--color-success-300)] to-[var(--color-success-400)] rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300"
            >
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
            <h3 class="text-2xl font-semibold text-[var(--color-gray-900)] mb-3">업무일지</h3>
            <p class="text-[var(--color-gray-600)] leading-relaxed">
              일일 업무 진행 상황을 기록하고 팀원들과 공유하여 투명하게 소통하세요.
            </p>
          </div>

          <!-- Todo 목록 -->
          <div
            class="group bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl border border-[var(--color-warning-100)] p-8 hover:shadow-2xl hover:border-[var(--color-warning-300)] transition-all duration-300 cursor-pointer transform hover:-translate-y-2"
            @click="$router.push('/todos')"
          >
            <div
              class="w-16 h-16 bg-gradient-to-br from-[var(--color-warning-300)] to-[var(--color-warning-400)] rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300"
            >
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
                />
              </svg>
            </div>
            <h3 class="text-2xl font-semibold text-[var(--color-gray-900)] mb-3">Todo 목록</h3>
            <p class="text-[var(--color-gray-600)] leading-relaxed">
              개인 및 팀의 할 일을 체계적으로 관리하고 우선순위를 설정하여 목표를 달성하세요.
            </p>
          </div>

          <!-- 타임캡슐 -->
          <div
            class="group bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl border border-[var(--color-info-100)] p-8 hover:shadow-2xl hover:border-[var(--color-info-300)] transition-all duration-300 cursor-pointer transform hover:-translate-y-2"
            @click="$router.push('/timecapsule')"
          >
            <div
              class="w-16 h-16 bg-gradient-to-br from-[var(--color-info-500)] to-[var(--color-info-600)] rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300"
            >
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <h3 class="text-2xl font-semibold text-[var(--color-gray-900)] mb-3">타임캡슐</h3>
            <p class="text-[var(--color-gray-600)] leading-relaxed">
              미래의 팀에게 격려의 메시지나 중요한 기록을 남겨보세요.
            </p>
          </div>

          <!-- 룰렛 -->
          <div
            class="group bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl border border-[var(--color-error-100)] p-8 hover:shadow-2xl hover:border-[var(--color-error-300)] transition-all duration-300 cursor-pointer transform hover:-translate-y-2"
            @click="$router.push('/roulette')"
          >
            <div
              class="w-16 h-16 bg-gradient-to-br from-[var(--color-error-300)] to-[var(--color-error-400)] rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300"
            >
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
                />
              </svg>
            </div>
            <h3 class="text-2xl font-semibold text-[var(--color-gray-900)] mb-3">룰렛</h3>
            <p class="text-[var(--color-gray-600)] leading-relaxed">
              점심 메뉴 선택, 담당자 지정 등 팀 활동과 의사결정을 재미있게 만들어보세요.
            </p>
          </div>

          <!-- 채팅 -->
          <div
            class="group bg-white/90 backdrop-blur-sm rounded-3xl shadow-xl border border-[var(--color-secondary-100)] p-8 hover:shadow-2xl hover:border-[var(--color-secondary-300)] transition-all duration-300 cursor-pointer transform hover:-translate-y-2"
            @click="$router.push('/chat')"
          >
            <div
              class="w-16 h-16 bg-gradient-to-br from-[var(--color-secondary-main)] to-[var(--color-secondary-400)] rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300"
            >
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
                />
              </svg>
            </div>
            <h3 class="text-2xl font-semibold text-[var(--color-gray-900)] mb-3">팀 채팅</h3>
            <p class="text-[var(--color-gray-600)] leading-relaxed">
              실시간으로 팀원들과 소통하고 아이디어를 공유하세요.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { useRouter } from 'vue-router';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const router = useRouter();
</script>
