package io.github.rcarlosdasilva.wenger.ms.handler.cloud

import org.springframework.stereotype.Component

/**
 * 用户主体代理，通过该类可在任何类中随时获取主体信息
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@Component
class PrincipalHandler {

  private val t = ThreadLocal<Principal>()

  fun get() = t.get() ?: NONE_PRINCIPAL

  fun set(principal: Principal) = t.set(principal)

  fun clean() = t.remove()

  data class Principal(
    val id: Long,
    val username: String,
    val code: String,
    val token: String?
  )

  companion object {
    internal val NONE_PRINCIPAL = Principal(-1, "", "", null)
  }

}