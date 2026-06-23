import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './styles/reset.scss'
import './styles/theme.scss'

const app = createApp(App)

app.config.errorHandler = (err, instance, info) => {
  console.error('=== Vue Error ===', err, info)
}
app.config.warnHandler = (msg, instance, trace) => {
  console.warn('=== Vue Warn ===', msg, trace)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
