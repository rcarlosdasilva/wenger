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

  override fun hashCode(): Int = Objects.hashCode(id, javaClass.name)

  override fun equals(other: Any?): Boolean = other != null && Objects.equal(hashCode(), other.hashCode())

  override fun toString(): String = MoreObjects.toStringHelper(this).add("id", id).toString()

  override fun pkVal(): Serializable? = id

  companion object {
    const val serialVersionUID = 3831204482160837640L
  }

}

/**
 * 系统Entity
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
abstract class SystemEntity<E : Model<*>> : BasicEntity<E>() {

  @TableField("flag_disabled")
  var isDisabled = false

  companion object {
    const val serialVersionUID = -5547146227019405990L
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
  var isDeleted = false
  @TableField(value = "time_create", fill = FieldFill.INSERT)
  var createAt: Date? = null
  @TableField(value = "time_update", fill = FieldFill.INSERT_UPDATE)
  var updateAt: Date? = null
  @TableField(value = "who_create", fill = FieldFill.INSERT)
  var createBy: Long? = null
  @TableField(value = "who_update", fill = FieldFill.INSERT_UPDATE)
  var updateBy: Long? = null

  companion object {
    const val serialVersionUID = 5776344955989370955L
  }

}