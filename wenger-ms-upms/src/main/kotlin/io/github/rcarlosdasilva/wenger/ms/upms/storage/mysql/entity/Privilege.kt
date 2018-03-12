package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BusinessEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 特权表，标识除角色之外的额外附加或削减权限
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_privilege")
class Privilege : BusinessEntity<Privilege>() {

  @TableField("account_id")
  var accountId: Long? = null
  @TableField("cluster_id")
  var clusterId: Long? = null
  @TableField("authority_id")
  var authorityId: Long? = null
  /**
   * 特权类型，取值：AFX附加额外权限，CUT削减已有权限
   */
  var type: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}