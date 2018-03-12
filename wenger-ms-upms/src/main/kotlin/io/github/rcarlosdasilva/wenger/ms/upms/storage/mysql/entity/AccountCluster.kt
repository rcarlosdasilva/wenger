package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.BasicEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("mid_account_cluster")
class AccountCluster : BasicEntity<AccountCluster>() {

  @TableField("account_id")
  var accountId: Long? = null
  @TableField("cluster_id")
  var clusterId: Long? = null

  companion object {
    const val serialVersionUID = 1L
  }

}