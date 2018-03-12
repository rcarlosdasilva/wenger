package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BasicEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("mid_role_authority")
class RoleAuthority : BasicEntity<RoleAuthority>() {

  @TableField("role_id")
  var roleId: Long? = null
  @TableField("authority_id")
  var authorityId: Long? = null

  companion object {
    const val serialVersionUID = 1L
  }

}