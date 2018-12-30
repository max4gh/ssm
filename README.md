# SSM 框架搭建及学习
## 软件列表及用途
- maven: 项目及包依赖管理
- mybatis: ORM管理及数据库连接查询中间件
- spring: 提供 Ioc 容器及依赖注入支持
- spring-mvc: MVC Web设计模式支持

## maven下载及安装
- 官方网站：[https://maven.apache.org/](https://maven.apache.org/)
- 下载页面: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
### 安装
下载保存至本机任意目录，解压之后将目录添加到系统path

检查是否安装成功
``` 
mvn -v
Apache Maven 3.6.0 (97c98ec64a1fdfee7767ce5ffb20918da4f719f3; 2018-10-25T02:41:47+08:00)
Maven home: /Users/Max/Work/apache-maven-3.6.0
Java version: 10.0.1, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/jdk-10.0.1.jdk/Contents/Home
Default locale: zh_CN_#Hans, platform encoding: UTF-8
OS name: "mac os x", version: "10.14", arch: "x86_64", family: "mac"
```
maven 的基本概念及说明文档，在[这里](https://maven.apache.org/guides/getting-started/index.html)

## mybatis
### 初闻 mybatis，先来感性认知
在 maven 项目中引入，修改项目pom.xml, 在依赖中添加以下代码：
```
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.4.6</version>
</dependency>
```
添加 ```mybatis-config.xml``` 全局配置，包括数据连接，映射文件，看下面的例子:
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/issm"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
      </dataSource>
    </environment>
  </environments>
  
  <!-- 映射文件 -->
  <mappers>
  	<mapper resource="com/corebao/mapper/RoleMapper.xml"/>
  </mappers>
</configuration>
```
以上文件要放在代码 ```src/main/java``` 目录中，方面代码可以访问到，稍后在创建```SqlSessionFactory```会看到对它的引用。

再看下映射文件的例子 ```RoleMapper.xml ```:
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.corebao.mapper.RoleMapper">
  <select id="getRole" parameterType="long" resultType="com.corebao.pojo.Role">
    select id, role_name as roleName, note from t_role where id = #{id}
  </select>
</mapper>
```
下面是使用它来查询数据库的例子:
```
public static void testDb()
    {
        SqlSessionFactory sqlSessionFactory = null;
        String resource = "mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            //通过 XML 生成 SqlSessionFactory
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            //通过 SqlSessionFactory 创建 SqlSession
            SqlSession sqlSession = sqlSessionFactory.openSession();
            //通过 SqlSession 发送 SQL
            Role role = (Role)sqlSession.selectOne("com.corebao.mapper.RoleMapper.getRole", 1L);
            //通过 Mapper 接口发送 SQL
            RoleMapper roleMapper = sqlSession.getMapper(RoleMapper.class);
            Role role2 = roleMapper.getRole(2L);
            System.out.println(role);
            System.out.println(role2.getRoleName());
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
```
可以看到我们要使用 mybatis 连接数据库和执行 SQL 查询需要要几个步骤：

1. 使用```SqlSessionFactoryBuilder```通过读取全局配置文件(```mybatis-config.xml```)生成一个```SqlSessionFactory```实例
2. 通过```SqlSessionFactory```实例获取一个```SqlSession```实例
3. 通过```SqlSession```实例进行数据库操作，有两种方式，一是 ```SqlSession``` 直接发送 SQL 语句，二是通过 ```Mapper``` 进行发送。

两种不同方式发送 ```SQL``` 的区别显而易见，直接通过 ```SqlSession``` 发送要写一长串的类名的，容易出现，不易调试；而通过 ```Mapper``` 的方式，则便捷很多，用 ```IDE``` 还可以有提示，也可以在编译阶段直接发现一些低级的错误。

### mapper
在上面的例子中，有出现过```mapper```,那是什么呢？```mapper``` 提供了```pojo```到数据库的映射，这就是大家识知的```ORM```了。为了说明，我们首先要有一个``` pojo```，来看上面例子中用到的 Role.java:
```
package com.corebao.pojo;

public class Role {
	private Long id;
	private String roleName;
	private String note;
	/*getter 和 setter 就省略了，实际中是需要加上的*/
}

```
然后我们还需要有一个```mapper```接口,所有的SQL都是通过```mapper```接口进行关联发送的，来看上例用到的```RoleMapper```:
```
package com.corebao.mapper;

import com.corebao.pojo.Role;

public interface RoleMapper {
	public Role getRole(Long id);
}
```
这个 ```mapper``` 只定义了一个接口 ```getRole```。

```Role``` 跟这个 ```RoleMapper``` 是怎么关联（映射）起来的呢？有两种方式，一又是通过经典（非常烦同时也很强大）的 ```XML``` 文件配置的方式，在我们的 ```mybatis-config.xml``` 我们注意到有一段：
```
  <!-- 映射文件 -->
  <mappers>
  	<mapper resource="com/corebao/mapper/RoleMapper.xml"/>
  </mappers>
```
对了, 就是它 ```RoleMapper.xml```。内容上面也有，可以看到，```getRole```接口的```SQL```是这样的：
```
select id, role_name as roleName, note from t_role where id = #{id}
```
数据库的```role_name```通过别名的方式关联到```pojo: Role``` 的```roleName```私有成员, ```#{id}```表示这是一个参数，对应着 ```RoleMapper```的```getRole```方法的参数，参数的类型为 ```Long```, 所以会看到发送SQL时这样的调用程现：
```
Role role = (Role)sqlSession.selectOne("com.corebao.mapper.RoleMapper.getRole", 1L);
``` 
注意第二个参数 ```1L```;

## 总结最基本的概念
- SqlSessionFactoryBuilder
- SqlSessionFactory
- SqlSession
- Mapper

## 通用常景
- ORM 自动映射
- CURD
- 关联关系
- 动态SQL
- 数据分页 RowBounds
- SQL 语句日志记录
- 事务处理
