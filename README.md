## 通信协议的报文图表（单位：Byte）

### 请求报文的格式

<table>
<thead>
<tr>
<th>字段</th>
<th>魔术值</th>
<th>版本号</th>
<th>头部长度</th>
<th>总长度</th>
<th>序列化类型</th>
<th>压缩方式</th>
<th>响应码</th>
<th>请求的id</th>
<th>请求体</th>
</tr>
</thead>
<tbody>
<tr>
<td>字节数</td>
<td>6</td>
<td>1</td>
<td>2</td>
<td>8</td>
<td>1</td>
<td>1</td>
<td>1</td>
<td>8</td>
<td>可变长度</td>
</tr>
</tbody>
</table>  

### 响应报文的格式

<table>
<thead>
<tr>
<th>字段</th>
<th>魔术值</th>
<th>版本号</th>
<th>头部长度</th>
<th>总长度</th>
<th>序列化类型</th>
<th>压缩方式</th>
<th>请求类型</th>
<th>请求的id</th>
<th>时间戳</th>
<th>请求体</th>
</tr>
</thead>
<tbody>
<tr>
<td>字节数</td>
<td>6</td>
<td>1</td>
<td>2</td>
<td>8</td>
<td>1</td>
<td>1</td>
<td>1</td>
<td>8</td>
<td>8</td>
<td>可变长度,若请求类型为心跳检测则没有请求体</td>
</tr>
</tbody>
</table>  

