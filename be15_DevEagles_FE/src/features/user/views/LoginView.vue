<template>
  <div class="login-container input-reset-scope">
    <form :class="['login-box', { shake }]" @submit.prevent="fetchUser">
      <img :src="Logo" alt="로고" class="logo" />

      <BaseInput
        v-model="params.username"
        label="아이디"
        type="text"
        placeholder="아이디를 입력해주세요."
        @focus="errorMessage = ''"
      />
      <BaseInput
        v-model="params.password"
        label="비밀번호"
        type="password"
        placeholder="비밀번호를 입력해주세요."
        :error="errorMessage"
        @focus="errorMessage = ''"
      />

      <div class="login-links">
        <a href="#" @click.prevent="showFindIdModal = true">아이디 찾기</a>
        <span>|</span>
        <a href="#" @click.prevent="showFindPwdModal = true">비밀번호 찾기</a>
      </div>

      <div class="login-buttons">
        <a class="btn btn-primary" @click.prevent="goToSignup">회원가입</a>
        <button class="btn btn-primary" type="submit">로그인</button>
      </div>
    </form>
  </div>
  <BaseModal v-model="showVerifyModal" title="미인증 회원">
    <p class="modal-text">
      인증되지 않은 회원입니다.<br />
      이메일을 인증하시겠습니까?
    </p>

    <template #footer>
      <div class="modal-footer-buttons">
        <BaseButton type="error" @click="showVerifyModal = false">취소</BaseButton>
        <BaseButton type="primary" @click="goVerifyEmail">확인</BaseButton>
      </div>
    </template>
  </BaseModal>

  <FindIdModal v-model="showFindIdModal" @submit="onFindIdSubmit" />
  <FindPwdModal v-model="showFindPwdModal" @submit="onFindPwdSubmit" />

  <BaseModal v-model="showFindIdResModal" title="">
    <div class="modal-body center-content">
      <template v-if="isFoundId">
        <p>
          <strong>회원님의 아이디는</strong><br />
          {{ foundUserId }} <strong>입니다.</strong>
        </p>
      </template>
      <template v-else>
        <p>
          존재하지 않는 회원정보입니다.<br />
          확인 후 다시 입력해주세요.
        </p>
      </template>
    </div>
    <template #footer>
      <BaseButton type="primary" @click="showFindIdResModal = false">확인</BaseButton>
    </template>
  </BaseModal>

  <BaseModal v-model="showFindPwdResModal" title="">
    <div class="modal-body center-content">
      <template v-if="isFoundPwd">
        <p>
          비밀번호 변경을 위한 인증 메일이 전송되었습니다.<br />
          인증 완료 후 비밀번호를 변경해주세요.
        </p>
      </template>
      <template v-else>
        <p>
          존재하지 않는 회원정보입니다.<br />
          확인 후 다시 입력해주세요.
        </p>
      </template>
    </div>
    <template #footer>
      <BaseButton type="primary" @click="showFindPwdResModal = false">확인</BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { useAuthStore } from '@/store/auth.js';
  import {
    findUserId,
    findUserPwd,
    login,
    sendAuth,
    validUserStatus,
  } from '@/features/user/api/user.js';
  import Logo from '/assets/image/logo-goody-with-text.png';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import { setupChat } from '@/features/chat/config/chatConfig.js';
  import FindIdModal from '@/features/user/components/FindIdModal.vue';
  import FindPwdModal from '@/features/user/components/FindPwdModal.vue';

  const router = useRouter();
  const authStore = useAuthStore();
  const params = ref({
    username: '',
    password: '',
  });

  const errorMessage = ref('');
  const shake = ref(false); // 🔥 shake 트리거
  const showVerifyModal = ref(false);
  const showFindIdModal = ref(false);
  const showFindPwdModal = ref(false);
  const showFindIdResModal = ref(false);
  const showFindPwdResModal = ref(false);

  const isFoundId = ref(false);
  const foundUserId = ref('');

  const onFindIdSubmit = async ({ userName, phoneNumber }) => {
    try {
      const res = await findUserId({ userName, phoneNumber });
      showFindIdModal.value = false;
      if (res.data.success && res.data.data) {
        foundUserId.value = res.data.data.email;
        isFoundId.value = true;
      } else {
        isFoundId.value = false;
      }
      showFindIdResModal.value = true;
    } catch (e) {
      console.error(e);
      isFoundId.value = false;
      showFindIdResModal.value = true;
    }
  };

  const isFoundPwd = ref(false);

  const onFindPwdSubmit = async ({ userName, email }) => {
    try {
      await findUserPwd({ userName, email });
      isFoundPwd.value = true;
    } catch (e) {
      console.error(e);
      isFoundPwd.value = false;
    } finally {
      showFindPwdResModal.value = true;
    }
  };

  const fetchUser = async () => {
    try {
      const res = await login(params.value);

      await authStore.setAuth(res.data.data.accessToken);
      // refreshToken은 HttpOnly 쿠키로 자동 설정됨

      const res_valid = await validUserStatus();
      const isValid = res_valid.data.data;

      if (!isValid) {
        showVerifyModal.value = true;
        return;
      }

      // 로그인 성공 후 홈으로 이동 (채팅 초기화는 auth.js에서 처리)
      await router.push('/');
    } catch (error) {
      console.error('로그인 실패 : ', error);
      errorMessage.value = '존재하지 않는 회원정보입니다.';
      shake.value = false;
      requestAnimationFrame(() => {
        shake.value = true;
      });
    }
  };

  const goVerifyEmail = async () => {
    showVerifyModal.value = false;
    await sendAuth({ email: params.value.username });
  };

  const goToSignup = () => {
    router.push('/signup');
  };
</script>

<style scoped>
  .login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: var(--color-gray-50);
  }

  .login-box {
    background-color: var(--color-neutral-white);
    border: 1px solid var(--color-gray-200);
    border-radius: 8px;
    padding: 40px;
    width: 400px;
    box-shadow: var(--shadow-drop);
    transition: transform 0.2s ease-in-out;
  }

  .logo {
    display: block;
    margin: 0 auto 24px;
    height: 64px;
  }

  .login-links {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    font-size: 12px;
    color: var(--color-gray-600);
    margin-top: 8px;
    margin-bottom: 24px;
  }

  .login-links a {
    color: var(--color-gray-700);
    text-decoration: none;
  }

  .login-buttons {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  .input-reset-scope :deep(.input-error) {
    border-color: inherit !important;
    background-color: inherit !important;
    box-shadow: none !important;
  }

  .modal-text {
    text-align: center;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.5;
    padding: 16px 0;
  }

  .modal-footer-buttons {
    display: flex;
    justify-content: center;
    gap: 12px;
  }
</style>
