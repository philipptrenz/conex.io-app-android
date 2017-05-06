/**
 * conex.io REST-Like API
 * The conex.io API provides the functionality to interact with home automation devices, which are connected to a home automation server, detached from the manufacturer specific communication syntax.
 *
 * OpenAPI spec version: 0.9.5
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.swagger.client.model;

import io.swagger.client.model.Function;
import java.util.Date;
import io.swagger.annotations.*;
import com.google.gson.annotations.SerializedName;

/**
 * Generic slider switch
 **/
@ApiModel(description = "Generic slider switch")
public class Dimmer extends Function {

  //@SerializedName("function_id") - Has to be commented out for polymorphic object mapping via GSON and RuntimeTypeAdapterFactory.java
  //private String functionId = null;
  @SerializedName("value")
  private Integer value = null;
  @SerializedName("timestamp")
  private Date timestamp = null;

  /**
   *
  @ApiModelProperty(required = true, value = "")
  public String getFunctionId() {
    return functionId;
  }
  public void setFunctionId(String functionId) {
    this.functionId = functionId;
  }
  */
  /**
   * minimum: 0
   * maximum: 255
   **/
  @ApiModelProperty(value = "")
  public Integer getValue() {
    return value;
  }
  public void setValue(Integer value) {
    this.value = value;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  public Date getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Dimmer dimmer = (Dimmer) o;
    return (this.getFunctionId() == null ? dimmer.getFunctionId() == null : this.getFunctionId().equals(dimmer.getFunctionId())) &&
        (this.value == null ? dimmer.value == null : this.value.equals(dimmer.value)) &&
        (this.timestamp == null ? dimmer.timestamp == null : this.timestamp.equals(dimmer.timestamp));
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (this.getFunctionId() == null ? 0: this.getFunctionId().hashCode());
    result = 31 * result + (this.value == null ? 0: this.value.hashCode());
    result = 31 * result + (this.timestamp == null ? 0: this.timestamp.hashCode());
    return result;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Dimmer {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  functionId: ").append(getFunctionId()).append("\n");
    sb.append("  value: ").append(value).append("\n");
    sb.append("  timestamp: ").append(timestamp).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
