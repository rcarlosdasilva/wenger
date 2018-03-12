package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BusinessEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 角色表，角色只隶属于一个用户组下
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_role")
class Role : BusinessEntity<Role>() {

  @TableField("cluster_id")
  var clusterId: Long? = null
  var name: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}