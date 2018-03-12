package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.SystemEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 功能操作表
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_feature")
class Feature : SystemEntity<Feature>() {

  @TableField("menu_id")
  var menuId: Long? = null
  @TableField("authority_id")
  var authorityId: Long? = null
  /**
   * 摘要，用于做权限判断
   */
  var digest: String? = null
  var description: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}