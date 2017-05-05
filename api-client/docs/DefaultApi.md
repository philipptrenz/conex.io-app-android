# DefaultApi

All URIs are relative to *http://localhost:8080/v0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**devicesPatch**](DefaultApi.md#devicesPatch) | **PATCH** /devices | 
[**devicesPost**](DefaultApi.md#devicesPost) | **POST** /devices | 
[**functionsPost**](DefaultApi.md#functionsPost) | **POST** /functions | 
[**groupsPost**](DefaultApi.md#groupsPost) | **POST** /groups | 
[**roomsPost**](DefaultApi.md#roomsPost) | **POST** /rooms | 


<a name="devicesPatch"></a>
# **devicesPatch**
> devicesPatch(patcher)



Modifies the state of a single &#x60;device&#x60; or group of &#x60;devices&#x60;. 

### Example
```java
// Import classes:
//import io.swagger.client.api.DefaultApi;

DefaultApi apiInstance = new DefaultApi();
Patcher patcher = new Patcher(); // Patcher | Filter object with function values
try {
    apiInstance.devicesPatch(patcher);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#devicesPatch");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **patcher** | [**Patcher**](Patcher.md)| Filter object with function values |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="devicesPost"></a>
# **devicesPost**
> Devices devicesPost(filter)



Get a list of &#x60;device&#x60; objects based on the specified filter. 

### Example
```java
// Import classes:
//import io.swagger.client.api.DefaultApi;

DefaultApi apiInstance = new DefaultApi();
Filter filter = new Filter(); // Filter | The user specified filter
try {
    Devices result = apiInstance.devicesPost(filter);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#devicesPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | [**Filter**](Filter.md)| The user specified filter |

### Return type

[**Devices**](Devices.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="functionsPost"></a>
# **functionsPost**
> Ids functionsPost(filter)



Get a list of function ID&#39;s. 

### Example
```java
// Import classes:
//import io.swagger.client.api.DefaultApi;

DefaultApi apiInstance = new DefaultApi();
Filter filter = new Filter(); // Filter | The user specified filter
try {
    Ids result = apiInstance.functionsPost(filter);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#functionsPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | [**Filter**](Filter.md)| The user specified filter |

### Return type

[**Ids**](Ids.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="groupsPost"></a>
# **groupsPost**
> Ids groupsPost(filter)



Get a list of group ID&#39;s. 

### Example
```java
// Import classes:
//import io.swagger.client.api.DefaultApi;

DefaultApi apiInstance = new DefaultApi();
Filter filter = new Filter(); // Filter | The user specified filter
try {
    Ids result = apiInstance.groupsPost(filter);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#groupsPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | [**Filter**](Filter.md)| The user specified filter |

### Return type

[**Ids**](Ids.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="roomsPost"></a>
# **roomsPost**
> Ids roomsPost(filter)



Get a list of room ID&#39;s. 

### Example
```java
// Import classes:
//import io.swagger.client.api.DefaultApi;

DefaultApi apiInstance = new DefaultApi();
Filter filter = new Filter(); // Filter | The user specified filter
try {
    Ids result = apiInstance.roomsPost(filter);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#roomsPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | [**Filter**](Filter.md)| The user specified filter |

### Return type

[**Ids**](Ids.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

