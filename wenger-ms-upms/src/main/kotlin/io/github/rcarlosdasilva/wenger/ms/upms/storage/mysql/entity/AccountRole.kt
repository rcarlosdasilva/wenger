package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BasicEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 用户在特定组下的角色（一个用户在一个组下只关联一个角色）
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("mid_account_role")
class AccountRole : BasicEntity<AccountRole>() {

  @TableField("account_id")
  var accountId: Long? = null
  @TableField("role_id")
  var roleId: Long? = null

  companion object {
    const val serialVersionUID = 1L
  }

}