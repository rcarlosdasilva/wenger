package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BusinessEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 用户分组表
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_cluster")
class Cluster : BusinessEntity<Cluster>() {

  @TableField("parent_id")
  var parentId: Long? = null
  var name: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}