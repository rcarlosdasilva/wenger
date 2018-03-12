package io.github.rcarlosdasilva.wenger.ms.upms.storage.mysql.entity

import io.github.rcarlosdasilva.wenger.ms.arc.SystemEntity
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableName

/**
 * 权限表
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@TableName("upms_authority")
class Authority : SystemEntity<Authority>() {

  /**
   * 权限类型，取值：MNU菜单展示权限，CPN页内组件展示权限，FET功能操作权限
   */
  var type: String? = null

  companion object {
    const val serialVersionUID = 1L
  }

}