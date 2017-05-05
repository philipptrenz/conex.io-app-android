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
import java.util.*;
import io.swagger.annotations.*;
import com.google.gson.annotations.SerializedName;

@ApiModel(description = "")
public class Device {
  
  @SerializedName("device_id")
  private String deviceId = null;
  @SerializedName("type_id")
  private String typeId = null;
  @SerializedName("room_ids")
  private List<String> roomIds = null;
  @SerializedName("group_ids")
  private List<String> groupIds = null;
  @SerializedName("functions")
  private List<Function> functions = null;

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  public String getDeviceId() {
    return deviceId;
  }
  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  public String getTypeId() {
    return typeId;
  }
  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  public List<String> getRoomIds() {
    return roomIds;
  }
  public void setRoomIds(List<String> roomIds) {
    this.roomIds = roomIds;
  }

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  public List<String> getGroupIds() {
    return groupIds;
  }
  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  public List<Function> getFunctions() {
    return functions;
  }
  public void setFunctions(List<Function> functions) {
    this.functions = functions;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Device device = (Device) o;
    return (this.deviceId == null ? device.deviceId == null : this.deviceId.equals(device.deviceId)) &&
        (this.typeId == null ? device.typeId == null : this.typeId.equals(device.typeId)) &&
        (this.roomIds == null ? device.roomIds == null : this.roomIds.equals(device.roomIds)) &&
        (this.groupIds == null ? device.groupIds == null : this.groupIds.equals(device.groupIds)) &&
        (this.functions == null ? device.functions == null : this.functions.equals(device.functions));
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (this.deviceId == null ? 0: this.deviceId.hashCode());
    result = 31 * result + (this.typeId == null ? 0: this.typeId.hashCode());
    result = 31 * result + (this.roomIds == null ? 0: this.roomIds.hashCode());
    result = 31 * result + (this.groupIds == null ? 0: this.groupIds.hashCode());
    result = 31 * result + (this.functions == null ? 0: this.functions.hashCode());
    return result;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Device {\n");
    
    sb.append("  deviceId: ").append(deviceId).append("\n");
    sb.append("  typeId: ").append(typeId).append("\n");
    sb.append("  roomIds: ").append(roomIds).append("\n");
    sb.append("  groupIds: ").append(groupIds).append("\n");
    sb.append("  functions: ").append(functions).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
