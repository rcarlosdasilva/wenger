@file:Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")

package io.github.rcarlosdasilva.wenger.ms.arc

import com.baomidou.mybatisplus.activerecord.Model
import com.baomidou.mybatisplus.annotations.TableField
import com.baomidou.mybatisplus.annotations.TableLogic
import com.baomidou.mybatisplus.enums.FieldFill
import com.baomidou.mybatisplus.enums.FieldStrategy
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.google.common.base.MoreObjects
import com.google.common.base.Objects

import java.io.Serializable
import java.util.*

/**
 * 基础Entity，开发中基本可忽略
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
abstract class BasicEntity<E : Model<*>> : Model<E>() {

  @JsonSerialize(using = ToStringSerializer::class)
  var id: Long? = null

  override fun hashCode(): Int {
    return Objects.hashCode(id, javaClass.name)
  }

  override fun equals(obj: Any?): Boolean {
    return obj != null && Objects.equal(hashCode(), obj.hashCode())
  }

  override fun toString(): String {
    return MoreObjects.toStringHelper(this).add("id", id).toString()
  }

  override fun pkVal(): Serializable? {
    return id
  }

  companion object {
    var serialVersionUID = 3831204482160837640L
  }

}

/**
 * 系统Entity
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
abstract class SystemEntity<E : Model<*>> : BasicEntity<E>() {

  @TableField("flag_disabled")
  var disabled = false

  companion object {
    var serialVersionUID = -5547146227019405990L
  }

}

/**
 * 业务相关Entity
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
abstract class BusinessEntity<E : Model<*>> : SystemEntity<E>() {

  @TableLogic
  @TableField(value = "flag_deleted", strategy = FieldStrategy.IGNORED)
  var deleted = false
  @TableField(value = "time_create", fill = FieldFill.INSERT)
  var createAt: Date? = null
  @TableField(value = "time_update", fill = FieldFill.INSERT)
  var updateAt: Date? = null
  @TableField(value = "who_create", fill = FieldFill.INSERT_UPDATE)
  var createBy: Long? = null
  @TableField(value = "who_update", fill = FieldFill.INSERT_UPDATE)
  var updateBy: Long? = null

  companion object {
    var serialVersionUID = 5776344955989370955L
  }

}