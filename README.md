# orm
# ORM封装（JDBC封装）



# 一 目的

1. 强化基础知识
2. 强化编程思想



# 二 封装

* 将程序中经常用到的，比较复杂的代码，提取出来，形成一个独立的可以复用的工具。
* jdbc封装
  * 将jdbc执行的过程中公用代码进行一个提炼和处理，形成一个封装工具。
* orm封装
  * 体现在jdbc执行过程中。
  * ORM    
    * 对象  : java中的实体对象 装载一条数据 
    * 关系 ：数据库表 ， 存储数据  
    * 映射 ：
      * 将java实体中的数据映射到数据库表中  （添加， 删除，修改）
      * 将数据库表中的数据映射到java实体中 （ 查询）



# 三 原生jdbc操作

## 1 创建数据库表

```mysql
create database duyi_orm default charset utf8 ;

use duyi_orm ;

create table t_car(
    cno bigint primary key  auto_increment,
    cname varchar(16) unique,
    color varchar(16) not null,
    price double
);
```



## 2 创建实体

```java
public class Car {
    private Long cno ;
    private String cname ;
    private String color ;
    private Double price ;
}
```



## 3 创建Dao类



## 4 设计添加操作

* 定义启动类，提供要待添加的数据

* dao类**save方法**编写jdbc

  * 注意1： `com.mysql.cj.jdbc.Driver` 是mysql8以上驱动的路径 （cj）

  * 注意2：mysql数据库版本尽量与mysql驱动版本一致

    ​			 如果mysql数据库版本低， url需要配置 ` &useSSL=true `

  * 注意3：mysql驱动如果使用8.0.1x版本，url还需要配置`&serverTimezone=UTC`

    ​			 程序中用的8.0.2x版本不需要配置

  * 注意4：确保无论程序是否正常执行，都可以对stmt和conn进行关闭

    ​			 关闭代码应该写在finally中

  

## 5 设计查询操作

* 定义启动类，提供查询操作及数据输出打印
* dao类**findAll方法**编写jdbc，实现查询，并实现结果集组装。



## 6 获得自增主键值

* 在创建statement对象时，传递一个参数，表示要获得自增主键值。

  ```java
  stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS) ;
  ```

* 在执行sql后，通过结果集获得自增主键值

  ```java
  //5.5 获得自增主键值
  ResultSet rs = stmt.getGeneratedKeys();
  //理论上这个结果集中只有1条记录，且只有1个字段
  rs.next();
  long cno = rs.getLong(1);
  System.out.println("本次自增值为："+cno);
  ```

* **注意1：有时我们会批量保存数据，可以通过这个结果获得所有的数据的自增值**

  * 这种批量语句只有mysql支持。

  ```mysql
  insert into t_car(cname,color,price) values
      ('byd4','black',150000),
      ('byd5','black',160000),
      ('byd3','black',170000)
  ```

  ```java
  //jdbc code ....
  ResultSet rs = stmt.getGeneratedKeys();
  //理论上这个结果集中有3条记录， 每条记录只有1个字段
  while(rs.next()) {
      long cno = rs.getLong(1);
      System.out.println("本次自增值为：" + cno);
  }
  ```

* **注意2：如果保存失败，自增主键值依然会产生，并且不会回滚。 有时主键值不连续**。



## 7 事务处理

* 使用一个conn连接对象，创建多个stmt命令集对象，执行多个保存操作
* 默认情况下，这个连接对象中的所有数据库操作都是默认提交事务
* 所以执行一个条sql，就会提交一次事务，
* 所以一个conn，执行了多次操作，算作多个事务



* 可以调用`conn.setAutoCommit(fales)` 关闭自动提交

* 每次操作完成后，需要手动实现提交或回滚。

  `conn.commit()`

  `conn.rollback()`



# 四 框架封装

* 很容易想到，需要将jdbc的步骤封装起来
* 原来，想和数据库交互，需要自己编写jdbc代码
* 现在（封装后），想和数据库交互，只需要调用封装框架的api即可。



## 1 构建JdbcExecutor对象

* 该对象提供了对jdbc的基本功能实现
* 以后再想实现jdbc，只需要调用executor对象

```java
/**
 * jdbc执行器，封装了jdbc功能    <br/>
 * jdbc的主要功能是CRUD= 增删改 + 查
 */
public class JdbcExecutor {

    /**
     * 执行增删改操作
     */
    public void doUpdate(){}


    /**
     * 执行查询操作
     */
    public void doQuery(){}
}
```



## 2 设计doUpdate方法

* 使用jdbc来实现增删改操作

> **doUpdate实现**
>
> ```java
> /**
>      * 执行增删改操作<br/>
>      * 用户1： executor.doUpdate( "insert..values(?,?)" , 1,"bmw" ); <br>
>      * 用户2： executor.doUpdate( "delete..where cno=?" , 1); <br/>
>      * 注意：理论上  参数值数量，类型，逻辑顺序，应该与sql中的?匹配的。 如果不匹配，也是使用者传参问题，和executor没有关系 <br>
>      *      executor直接抛出异常
> */
> public int doUpdate(String sql,Object...params){
>     PreparedStatement stmt = null ;
>     try{
>         //1 引入驱动jar文件 (非编码实现)
> 
>         //2 加载驱动
>         //  未来每一次jdbc操作都会使用executor，对于多次的jdbc操作，加载驱动只需要做一次
>         //  可以在静态代码段中执行
> 
>         //3 创建连接
>         //  使用一次executor，就相当于执行了一次jdbc
>         //  一次jdbc就只能执行一次sql ？ 不是。可以执行多个sql
>         //  一次jdbc的多次sql执行，需要几个连接？   1个  （节省资源，可以事务处理）
>         //  未来使用executor，通过doUpdate执行一次增删改 再通过doQuery执行一次查询
>         //	都应该属于1个连接
>         //  可以在普通代码段或构造器中执行创建连接的操作
> 
>         //4 创建命令集对象
>         //  问题1：创建命令集时需要预处理sql ， 这个sql是不确定的
>         //		  未来不同的使用者使用executor执行操作时，应该会有不同的sql
>         //        所以这个sql应该以参数的形式传递进来
>         //  问题2：预处理sql时，可能有?。预处理结束后，需要为?传递对应的参数值
>         //        这个参数值是哪里来的呢？ 应该也是使用者传递的.
>         //        因为参数的类型和个数不确定，所以可以使用可变参数 Object...params
>         //           对外，使用者可以不传参，传1个参，传多个参
>         //           对内，无论使用者传递多少个参数，都是数组  length=0 , length=1 , length=n
>         stmt = conn.prepareStatement(sql);
>         //4.5 为预处理sql中的?传递参数
>         //    params[]
>         for(int i=0;i<params.length;i++){
>             stmt.setObject(i+1,params[i]);
>         }
> 
>         //5 执行sql
>         //  返回值为此次增删改操作的记录数。
>         //  未来会有很多使用者调用当前的方法来执行增删改
>         //  我们不确定是不是所有的使用者都不需要这个返回值
>         //  可能有人需要，有人不需要，所以必须返回这个值。
>         int count = stmt.executeUpdate();
> 
>         //6 各种关闭
>         //  stmt + conn
>         //  无论此次jdbc执行成功与否，都应该将资源释放。应该在finally中释放
>         //      随着未来对连接池的使用，这个释放就有两种含义 （关闭 ， 归还）
> 
>         return count ;
>     }catch(Exception e){
>         throw new RuntimeException(e);
>     }finally {
>         //目前我们只写了doUpdate，但未来我们实现doQuery时，还需要实现这个关闭
>         //所以可以考虑将其封装成一个方法
>         close(stmt);
>     }
> }
> ```



> **多次执行jdbc，只需要一次加载驱动**
>
> ```java
> static{
>      try {
>          Class.forName("com.mysql.cj.jdbc.Driver") ;
>      } catch (ClassNotFoundException e) {
>          throw new RuntimeException(e);
>      }
>  }
> ```
>
> 



> **一个Executor对象关联一个连接对象，可以执行多次sql命令操作**
>
> ```java
> //未来会存在
> DataSource pool ;
> Connection conn ;
> {
>  //未来我们可能会使用数据库连接池获得连接
>  //  数据库连接比较占资源，创建连接本身也是一个比较耗时的过程
>  //  在没有任何管理的情况下，每次jdbc都需要创建一个连接，每次jdbc结束后，都会关闭回收连接。
>  //  性能低，资源利用率不高。
>  //  可以通过连接池提前创建连接，当我们使用连接时，连接早就创建完了，只需要从连接池获得就可以了
>  //  使用完毕后，可以将连接归还给连接池，下次在进行分配复用。
>  conn = getConnection();
> }
> 
> private Connection getConnection(){
>  //判断，如果存在连接池，就从连接池中获得连接。 如果没有连接池，就创建连接
>  try {
>      if(pool != null){
>          return pool.getConnection() ;
>      }else {
>          return DriverManager.getConnection(
>              "jdbc:mysql://localhost:3306/duyi_orm?characterEncoding=utf8",
>              "root",
>              "root");
>      }
>  } catch (SQLException e) {
>      throw new RuntimeException(e);
>  }
> }
> ```
>
> 



> **每次sql命令执行完毕，都需要关闭结果集，无论doUpdate还是doQuery**
>
> ```java
> private void close(Statement stmt){
>  //目前我们关闭的是连接和命令集
>  //未来我们可能会实现批量操作 ， 一个连接 执行了多个命令集sql
>  //每次执行完毕，应该只关闭命令集，不应该关闭连接。
>  //连接什么时候关闭？ 框架也不知道此次批量处理到底会执行几个命令集，只有使用者才知道
>  //所以应该提供一个对外的api，供使用者主动来关闭连接
>  if(stmt != null){
>      try {
>          stmt.close();
>      } catch (SQLException e) {
>          throw new RuntimeException(e);
>      }
>  }
> }
> ```
>
> 



> **一个executor有一个连接，可以执行多次sql命令，什么时候关闭连接需要使用者控制**
>
> ```java
> /**
>      * 供使用者主动关闭连接
> */
> public void close(){
>     if(conn != null ){
>         try {
>             conn.close();
>         } catch (SQLException e) {
>             throw new RuntimeException(e);
>         }
>     }
> }
> ```



## 3 【doUpdate扩展】 获得自增主键值

* 只需要创建stmt对象时，指定参数即可

* 但有的时候，即使是保存，也不需要自增主键值

* 再有，doUpdate还可以实现删除和修改，也不需要自增主键值

* 所以需要使用者来控制是否需要自增主键值 

* **为doUpdate方法增加boolean参数**

  ```java
  public int doUpdate(String sql,boolean isGeneratedKeys,Object...params){
     //....
      if(isGeneratedKeys){
          //需要获得自增主键值
          stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS) ;
      }else{
          stmt = conn.prepareStatement(sql);
      }
      //....
  }
  ```

* **在执行sql后，获得自增主键值。默认按多个自增主键值获取**

  ```java
  //executor提供了一个存储自增主键值的属性
  private List<Long> generatedKeys ;
  
  
  //5.5 有可能执行的是一个insert操作，并需要获得自增主键值
  if(isGeneratedKeys){
      //需要自增主键值
      //有可能产生多个自增主键值
      ResultSet rs = stmt.getGeneratedKeys();
      generatedKeys = new ArrayList<>();
      while(rs.next()){
          long id = rs.getLong(1);
          generatedKeys.add(id);
      }
  }
  ```

* **使用者如何获得自增主键值呢？**

  * doUpdate方法的返回值，返回的是增删改操作的记录数
  * 主键值就不能通过返回值来返回
  * 可以将主键值缓存在一个集合中，对外提供可以获得主键值的api （方法）

  ```java
  /**
       *
       * @return 数组。放置直接返回集合时，利用引用传递对原集合数据造成破坏（不完全）
  */
  public Long[] getGeneratedKeys(){
      return generatedKeys.toArray(new Long[]{}) ;
  }
  
  /**
       * 绝大多数的insert操作，都是单条的。
       * @return
  */
  public Long getGeneratedKey(){
      return generatedKeys.get(0);
  }
  ```

* **注意：**

  * 我们现在为doUpdate方法增加了一个boolean参数，目的是可以选择是否获得自增主键值
  * 大部分情况下，不需要获得自增主键值，尤其是delete和update
  * 但即是如此，每次调用doUpdate方法时，还必须要传递一个false，标识不需要。
  * 可以为doUpdate提供一个重载方法

  ```java
  /**
       * 重载方法：为删除，修改和不需要自增主键值的insert来使用的
       * @param sql
       * @param params
       * @return
  */
  public int doUpdate(String sql,Object...params){
      return doUpdate(sql,false,params) ;
  }
  ```

  

## 4 【doUpdate扩展】 事务处理

* 要实现事务处理，只有2部

  1. 创建连接时，设置手动或自动处理事务 。 调用`conn.setAutoCommit(boolean)`
  2. 在合适的位置，提交 或 回滚事务

* **使用者在使用executor时，对于事务的处理方式需要进行控制 ， 通过传参的方式**。

  * 由于获得了连接是在代码段中执行的，无法传参。
  * 所以改为构造器执行并传参

  ```java
  private boolean isAutoCommit ;
  public JdbcExecutor(boolean isAutoCommit){
      this.isAutoCommit = isAutoCommit ;
      conn = getConnection();
      try {
          conn.setAutoCommit(isAutoCommit);
      } catch (SQLException e) {
          throw new RuntimeException(e);
      }
  }
  ```

  * 注意：
    * 绝大多数的情况下，对于事务的处理都是一致的。
    * 我们希望，不用可以传递参数，默认效果即可。
    * 所以可以提供重载构造器

  ```java
  /**
   * 默认手动提交事务
   */
  public JdbcExecutor(){
      this(false);
  }
  ```

* **在什么位置处理事务呢？**

  * 使用事务的情况，一般是一个业务功能，需要执行n次sql操作。
  * 应该是在所有的sql操作执行完毕后，提交事务
  * 在某一个sql操作失败后，回滚事务
  * 问题是？ 对于executor而言，什么时候执行完毕，什么手出错？
  * 只有使用者，才知道什么时候执行完毕。
  * **所以应该为使用者提供控制事务处理的api**

  ```java
  public void commit(){
      try {
          if(!isAutoCommit && conn != null){
              conn.commit();
          }
      } catch (SQLException e) {
          throw new RuntimeException(e);
      }
  }
  
  public void rollback(){
      if(!isAutoCommit && conn != null){
          try {
              conn.rollback();
          } catch (SQLException e) {
              throw new RuntimeException(e);
          }
      }
  }
  ```

## 5 补充：可变参数的重载

* 随着executor的优化， 提供了2个doUpdate方法
* 因为2个方法都存在可变参数
* 尽管固定参数部分不同，但依然无法重载
  * 虽然idea没有提示错误，但编译运行时，java的编译器会提示错误。
* 此时在传递参数时，就必须传递数组

```java
public void savesByExecutor(List<Car> cars ){
    JdbcExecutor executor = new JdbcExecutor(false);
    String sql = "insert into t_car(cname,color,price) values(?,?,?)" ;
    for(Car car : cars){
        executor.doUpdate(
            sql,
            true,
            new Object[]{car.getCname(),car.getColor(),car.getPrice()});
        System.out.println(executor.getGeneratedKey());
    }
    executor.commit();
}
```



## 6 设计doQuery方法

* 实现查询功能的jdbc操作

* 对于查询而言， 需要将结果集（表）中的数据 转换处理成 java需要的类型  （**ORM**）

* **分析1：**

  * 结果集中可能没有记录，可能有1条记录，可能有多条记录。
  * **最终返回的类型一定是List**  （null , size=0 ; size=1 ; size=n)
  * 为什么不直接返回ResultSet结果集呢？
    1. 解耦。   业务程序与jdbc对象解耦
    2. 从逻辑设计而言， 结果集使用完毕后需要关闭。如果在executor关闭了，rs返回给业务程序后就无法使用了
    3. 如果不关闭，因为不确定业务程序何时使用数据，就会导致结果集一直处于联通状态，耗资源。

* **分析2：**

  * 无论结果集中有没有数据，有多少数据，有什么类型的数据，最终存储在List集合中是没有问题的
  * **问题是，每条记录应该组成什么类型对象？**
    * t_car 表 --》Car
    * t_student表 -》Student
    * t_student join t_teacher  -》 Map
    * select count(*) from table  -》int , Integer , long , Long , String
  * 具体组成什么类型的对象，需要使用者来控制 （传参）

  ```java
  /**
       * 执行查询操作 <br/>
       * 第一个<T> 表示定义泛型，表示当前这个方法中，有一个类型不确定，暂时使用T来表示 <br/>
       * 哪个类型不确定呢？<br/>
       *  传参时指定的结果集中每条记录需要组成的对象类型不确定（Class<？>） ， 自然返回值List集合中每一个元素的类型也不确定（List<？>)<br/>
       *  但能确定的时，传参时，指定的rowtype是什么类型，最终返回值List中的对象就是什么类型<br/>
       * 第二个也就是List后面<T>，表示使用泛型 <br/>
       *  返回值List集合中的元素类型不确定，暂时使用之前定义的T来表示
       *
  */
  public <T> List<T> doQuery(String sql,Class<T> rowType,Object...params){}
  ```

* **分析3：如何将结果集中的每一条记录组成 T 对象呢？**

  * 设计一个ResultHandler对象，来专门负责实现结果集处理 （orm）

  * 目前我们主要提供3种类型的结果处理

    1. **简单类型**  int ， Integer ， long， Long ， double , Double , String ， date

       * **一条记录只存储一个值**
       * `select count(*) from t_car`
       * `select cno from t_car`
       * 注意： 与几条记录无关，我们关心的是一条记录有几个值
       * getInt() , getLong()

    2. **Map类型**

       * 一条记录有多个值，但没有与其对应的对象来存储，就可以使用map
       * `select count(*),max(price),min(price),avg(price), from t_car`
       * 处理机制：

       ```txt
       获得结果集的元数据
       通过元数据一次获得字段的名称 和 类型
       根据字段名和字段类型，从结果集中获得对应的值
       以字段名为key装入map
       ```

       

    3. **domain类型**

       * 一条记录有多个值，并且符合一个整体的含义。就可以使用实体对象
       * `select cno,cname,color,price from t_car` ---> Car
       * 由于domain类型没有一个明确的判断机制，优先判断前2种，再else就是表示第三种
       * 处理机制：

       ```txt
       利用反射创建domain对象
       利用反射获得所有的方法，并获得所有的set方法
       根据set方法获得对应的属性名  setCno --> cno , setC --> c
       根据set方法的参数列表获得参数类型 setCno(Long cno) --> Long.class
       根据属性名和类型，从结果集中获得对应的数值  rs.getLong(cno)
       利用反射，调用set方法完成对象的赋值 
       ```

       

## 7 【doQuery优化】自定义映射关系

> **1 对象属性没有对应的结果集字段**
>
> 原来在getxx(key)获得结果集数据时，如果key对应的字段名不存在，会抛出异常
>
> 我们可以通过try-catch，消除这个异常，同时给出一个警告信息，提示用户这里出现了不匹配的情况。让用户自行考虑。
>
> ```java
> private Object getValueByType(ResultSet rs , String key , Class type) {
>  try {
>      //...
>  }catch (SQLException e){
>      //我们认为，上述代码产生sql异常的原因就是 无法根据key找到对应的结果集字段
>      //我们认为也没有任何问题,给以提示
>      System.out.println("[warning] Column '"+key+"' not found");
>  }
>  return null ;
> }
> ```



> **2 自定义对象属性与字段的对应关系**
>
> * 在结果集组装成domain对象时，默认会根据属性名，作为对应的字段名来获取结果集数据
>   * Car.cno = rs.getLong("cno") ;
> * 有时，对象的属性名与逻辑上对应的字段名不匹配
>   * Car.carName = rs.getString("car_name")
> * 可以由框架提供一个注解 供使用者来声明 属性与字段对应关系。
>
> ```java
> //框架提供注解（语法规则）
> @Target({ElementType.METHOD,ElementType.FIELD})
> @Retention(RetentionPolicy.RUNTIME)
> public @interface Column {
>     /**
>      * 指定当前属性的对应的字段名
>      * @return
>      */
>     String value();
> }
> 
> ```
>
> ```java
> //使用者使用框架提供的规则向框架提供信息
> //可以作用在属性上，也可以作用在set方法上， 二者有其一即可
> public class Car {
>     //other properties...
> 
>     @Column("car_name")
>     private String cname ;
> 
>     @Column("car_name")
>     public void setCname(String cname){
>         this.cname = cname ;
>     }
>     //other methods ....
> }
> ```
>
> ```java
> //框架根据使用者定通过注解提供的信息获得最终的字段名
> 
> //后增加部分：
> //  原来我们认为对应的字段名就是属性名，属性名是由set方法变形而来。
> //  现在这个字段名可能有2个来源
> //      1. 和之前一样，是通过set方法变形获得的属性名
> //      2. 就是利用新增的@column注解直接指定的字段名
> //  所以现在需要先判断，有没有使用@column注解指定字段名， 如果没有，在通过set方法变形获得属性名
> String key  ;
> Column annotation = method.getAnnotation(Column.class);
> if(annotation != null){
>     //表示在set方法使用了column注解，指定了对应的字段名
>     key = annotation.value();
> }else{
>     //表示set方法没有column注解
>     //需要注意，set方法上没有注解，对应的属性上还可能有这个注解
>     //所以还需要检查属性上是否有这个注解
>     //要想检查属性，就需要先获得属性对象。 要想获得属性对象，需要先属性名。通过set方法变形获得
>     key = mname.substring(3); //Cno  (CarNo --> carNo)
>     if(key.length()==1){
>         //C --> c
>         key = key.toLowerCase();
>     }else{
>         //Cno --> cno
>         key = key.substring(0,1).toLowerCase() + key.substring(1);//C->c + no
>     }
>     //代码至此，就通过set方法，获得了对应的属性名  setCno --> cno , setCname --> cname
>     //一般属性都是私有的，使用getDeclaredField方法
>     Field field = rowType.getDeclaredField(key);
>     annotation = field.getAnnotation(Column.class);
>     if(annotation != null){
>         //属性上设置了这个注解， 使用指定的字段名
>         key = annotation.value();
>     }else{
>         //没有注解，就是用属性名作为字段名，而这个属性名就是key
>     }
> }
> ```
>
> 



> **3 将结果集简单类型的判断处理 和 结果集组成domain时属性赋值时的类型判断处理 合二为一** 
>
> ```java
> private Object getValueByType(ResultSet rs , Object key , Class type) throws SQLException {
>  try {
>      if (type == int.class || type == Integer.class) {
>          if(key instanceof String) {
>              //说明domain处理时的属性类型判断与取值
>              return rs.getInt((String)key);
>          }else{
>              //说明结果集处理成简单类型是的判断和取值
>              return rs.getInt((int)key);
>          }
>      }
> 		// other if ...
>  }catch (SQLException e){
>      //我们认为，上述代码产生sql异常的原因就是 无法根据key找到对应的结果集字段
>      //我们认为也没有任何问题,给以提示
>      System.out.println("[warning] Column '"+key+"' not found");
>  }
> 	//含义一：结果集处理成简单类型时，返回null，表示没有处理成功，说明结果集不能处理时简单类型
>  //	     要么是map，要么是domain
>  //含义二：结果集处理成domain类型时，根据属性名找对应的结果集字段值，结果没有找到
>  //       说明对象这个属性没有对应的结果集字段
>  return null ;
> }
> ```
>
> 





## 8 新sql规则

* 我们在之前执行保存操作时，因为sql中有很多的? 

* 所以在使用executor执行sql时，需要为这些问号传递对应的参数 （组成数组）

  ```java
   public void saveByExecutor(Car car){
       String sql = "insert into t_car(cname,color,price) values(null,?,?,?)" ;
  	 //other code ...
       executor.doUpdate(
           sql,
           true,
           new Object[]{car.getCname(),car.getColor(),car.getPrice()}) ;
   }
  ```

* 目前我们认为有2个不足的地方：

  1. sql语句中使用了许多的? 不方便理解，可读性差
  2. sql中?对应的数据都在car对象中，但执行时需要我们**按照逻辑**，从对象中**取出对应的属性值**，并**组成数组**

* 那我们简单思考，能不能直接将car对象传递给executor呢?  毕竟需要的数据在car中都有

  * 在底层可以通过反射获得属性值
  * 获得属性值后，也可以组成数组
  * **注意：如果要交给底层来实现这个操作，那按照什么顺序取出属性值组成数组呢？**
    * 这个顺序正常来说需要和sql中?的含义匹配

* 所以我们可以再考虑，在编写sql是，为?指定含义。这样底层就可以根据sql中指定的含义顺序获取数据。

* 只需要指定每一个?对应的属性名即可

  **例如：`insert into t_car(cname,color,price) values(null,?cname,?color,?price)`**

  为?指定名称的规则可以任意，只要达到需求即可。

  我们参考未来mybatis框架的设计 ，使用**#{cname}**  代替上面的 ?cname

* **最终我们的sql新规则如下：**

  ```java
  //sql="insert into t_car(cname,color,price) values(null,?,?,?)"
  sql="insert into t_car(cname,color,price) values(null,#{cname},#{color},#{price})"
      
  executor.doUpdate(
      sql,
      true,
      car) ;
  ```

## 9 预处理新SQL(分析)

* 框架提供了新sql，主要目的是让使用者在使用jdbc框架是，编写sql更简单，可读性更强。 传递参数更便捷。

* 在jdbc框架底层，最终执行sql命令，还是原生的jdbc操作

* 对于原生的jdbc而言，不认识新规则的sql

* **所以在框架底层需要将 新规则的sql 转换成 原始sql**

  `insert into t_car(cname,color,price) values(null,#{cname},#{color},#{price})`

  准换成

  `insert into t_car(cname,color,price) values(null,?,?,?)`

* **在转换中，需要将每一个?位置对应的参数名获得** ： cname ， color ， price  存入集合（数组）

* **再根据这些名称，从传递的参数对象中获得对应的参数值** ： bmw , red , 300000 存入集合（数组）

  * 分析一下，传递参数对象都有哪些可能

  1. 传递的是一个domain对象

     * executor.save("insert...values(#{cno},#{cname},#{color},#{price})" , car);

     * 使用反射技术，根据参数名 从对象中获得对应的值。

  2. 传递的是一个map对象 

     * executor.query("select...where price=#{price} and cno=#{cno}" , car/map{price,cno}) ;

     * 使用map的get方法，根据参数名key获得对应的值

  3. 传递的是一个简单类型值（int ,string ,date..） 

     * executor.delete("delete...where cno=#{cno}" , car/map/1001) ;
     * 此时无论#{key}中key叫什么名字，最终的值都是1001

  4. 扩展：传递的是一个数组 / List

     * executor.update("update..set color=#{0},cname=#{1} where cno=#{2}" , {"red" , "bmw" , 1001});

### 9.1 处理sql

* 将新规则sql处理成原始sql，并记存储参数名

  ```java
  public void executeSql(String sql){
      //循环遍历，找到每一组#{key}  截取出其中的key部分，同时在sql中将其替换成?
      //记录初始sql
      this.newSql = sql ;
      paramNames = new ArrayList<>();
      while(true){
          int i1 = sql.indexOf("#{") ;
          int i2 = sql.indexOf("}",i1) ;
          if(i1 == -1 || i2 == -1 || i1 > i2){
              //表示sql中没有完整的#{..}对 表示处理完毕
              break  ;
          }
          //代码至此，说明有成对的#{...}，同时还有如下信息
          //  1. i1左侧没有#{}
          //  2. i1和i2之间，就是key
          //  3. i2右边还有没有#{}不确定，需要继续判断
  
          //获取key，将其存入集合
          String paramName = sql.substring(i1 + 2, i2).trim();
          paramNames.add(paramName);
  
          //将#{}替换成?
          if(i2 == sql.length()-1) {
              //#{..}右侧没有内容，说明最后一个#{}也处理完毕，替换完?后就可以结束了
              sql = sql.substring(0, i1) + "?";
              break ;
          }else{
              //#{..}右侧还有内容，不确定是不是还有其他#{..}，所以需要继续判断
              sql = sql.substring(0,i1) + "?" + sql.substring(i2+1) ;
              continue;
          }
      }
      //代码至此，就将sql中所有的#{}都替换成了? ，同时也将参数名都存入了集合
      this.oldSql = sql ;
  }
  ```

* 扩展：利用`正则表达式`来实现上述sql的处理

  ```java
      final static Pattern pattern = Pattern.compile("#\\{\\w+\\}");
      public void executeSql(String sql){
          //循环遍历，找到每一组#{key}  截取出其中的key部分，同时在sql中将其替换成?  \w{3}
          //this.oldSql = sql.replaceAll("#\\{\\w+\\}","?") ;
          paramNames = new ArrayList<>();
          Matcher matcher = pattern.matcher(sql);
          while(matcher.find()) {
              //找到匹配正则的那组字符串 #{cname} #{color} #{price}
              String paramName = matcher.group() ; // #{xxx}
              paramName = paramName.replaceAll("[#\\{\\}]","");
              paramNames.add(paramName.trim());
          }
          this.oldSql = matcher.replaceAll("?");
          System.out.println("sql : " + oldSql);
          System.out.println("params : " + paramNames);
      }
  
  ```



### 9.2 处理参数对象

* 目前我们只处理3种参数对象  domain ，map ， 简单类型  （实际上不仅3中参数情况）

```java
public void executeParam(Object paramObj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if(paramObj == null){
        return ;
    }

    //有多少个参数，就应该有多少个参数值
    paramValues = new Object[paramNames.size()];

    if (isSimple(paramObj.getClass())) {
        for(int i=0;i<paramValues.length;i++) {
            //判断成立，说明是一个简单类型 （就是一个值 paramObj ==> 1001）
            //这个值就是当前的参数值
            paramValues[i] = paramObj;
        }
    }else if(paramObj instanceof Map){
        Map map = (Map) paramObj;
        for(int i=0;i<paramValues.length;i++) {
            String paramName = paramNames.get(i);
            paramValues[i] = map.get(paramName);
        }
    }else{
        Class c = paramObj.getClass();
        for(int i=0;i<paramValues.length;i++) {
            //代码至此，表示参数是一个domain类型
            //假设paramObj = car and paramName = cname  / c
            //==> car.getCname()
            String paramName = paramNames.get(i);
            //根据参数名，计算出对应的get方法名  get + C + name
            String methodName;
            if (paramName.length() == 1) {
                //c --> getC
                methodName = "get" + paramName.toUpperCase();
            } else {
                //cname --> getCname
                methodName = "get" + 
                    paramName.substring(0, 1).toUpperCase() + 
                    paramName.substring(1);
            }
            //代码至此，就得到get方法名了，可以通过反射获得方法
            //注意：如果方法不存在，会抛出异常
            Method method = c.getMethod(methodName);
            Object paramValue = method.invoke(paramObj);
            paramValues[i] = paramValue;
        }
    }
    System.out.println("values : " + Arrays.toString(paramValues));

}
```



## 10 SqlSession设计与实现

* executor只有doUpdate和doQuery两个方法 
* 尽管已经可以完成crud操作了
* 但随着executor对jdbc的封装，使用者只需要能够通过使用executor和数据库交互，并不需要必须对jdbc有深入的了解
* 在使用时，都是基于对数据库的操作需求，使用executor
* 此时发现只有2个和数据库交互方法，不易理解



* 我们就在应用程序和executor之间再增加一层（对象） ： **SqlSession**
* 这个一层来提供使用者更熟悉的CRUD API
* 有了SqlSession后，使用者不再直接使用executor了，而是使用sqlSession了

```java
/**
 * 提供与数据库交互相关的api
 */
public class SqlSession {
    public void insert(){}
    public void update(){}
    public void delete(){}
    public void select(){}
}
```

```java
 public void saveBySession(Car car){
     //准备将数据存入数据库
     String sql = "..." ;
     //原来自己写jdbc
     //后来使用executor工具
     //现在使用SqlSession
     SqlSession session = new SqlSession();
     session.insert();
     session.update();
     session.delete();
     session.select();
 }
```

### 10.1 基本结构设计

* SqlSession最终调用的还是executor

  * executor在创建时需要传递boolean控制事务

    ```java
    public class SqlSession {
        private JdbcExecutor executor;
        public SqlSession(){
            this(false);
        }
        public SqlSession(boolean isAutoCommit){
            executor = new JdbcExecutor(isAutoCommit);
        }
        //other method ...
    }
    ```

  * 通过session来控制事务的提交和回滚

    ```java
    public void commit(){
        executor.commit();
    }
    
    public void rollback(){
        executor.rollback();
    }
    ```

  * 最终使用者通过session来关闭会话（连接）

    ```java
    public void close(){
        executor.close();
    }
    ```

  * **获得自增主键的api暂时不考虑，后面有其他的实现方式**

### 10.2 insert方法设计

* **暂时不考虑获得自增主键情况，只执行sql，最终还是通过executor执行**，基本结构如下

  ```java
  public int insert(String sql,Object paramObj){
      return executor.doUpdate(sql,false,paramObj);
  }
  ```

* **由于内部调用的依然是executor.doUpdate方法，那就不仅仅可以执行insert操作，也可以是update等**

  * 也就是说使用者使用insert方法，执行一个update语句也是可以的
  * 对此我们可以做一些控制，利用自定义异常

  ```java
  public int insert(String sql,Object paramObj){
      if("insert".equalsIgnoreCase( sql.trim().substring(0,6) )) {
          //是一个insert语句
          return executor.doUpdate(sql, false, paramObj);
      }
      //代码至此，说明不是一个insert语句，不执行。 需要给使用者反馈（异常）
      throw new SQLFormatException("not a insert statement : " + sql);
  }
  ```

* **使用者在执行insert语句时，有可能传参，也可能不传参。我们可以提供一个重载方法来适应不同的情况**

  ```java
  public int insert(String sql,Object paramObj){}
  
  public int insert(String sql ){}
  ```

* **代码至此， update和delete方法应该和当前的insert有相同的结果**

  * 代码略...

* **接下来实现对自增主键值获取**

  * 之前有两种获得自增主键值的情况 （只获得1个，获得一堆）

  * 什么时候可以获得一堆呢？ 

    * 利用insert...values(),(),()语句可以实现一条语句保存多条记录，从而产生多个自增主键值
    * 首先这个sql不是通用语句，例如 oracle不支持这个语法
    * 其次，为这种sql传递参数以及匹配参数值也比较麻烦
    * **所以我们的sqlSession不支持批量保存，也就是不支持获得多个自增主键值**

  * **我们只实现单一主键值的获取**

    * 单一主键值，应该就只保存一条记录，一条记录应该传递一个car对象/map

    * 我们可以将主键值，直接存储在car对象的cno中（map中）

    * session怎么知道应该把主键值存储在对象的哪个属性中呢？

    * 使用者在调用insert方法时，可以通过传参来指定主键值存储的对象属性。同时也表示需要获得自增主键值

      `session.insert(sql,paramObj,"cno")` ;

      * 含义1： 此次保存成功后，需要获得自增主键值
      * 含义2： 将获得的自增主键值存储在paramObj的cno属性中（setCno)

### 10.3 select方法设计

* 目前我们通过doQuery来实现查询时，查询结果返回的一定是List集合 （0,1,n)

* 但实际开发中，有些业务逻辑可以明确的知道结果应该是0或1  比如：findById

  * 在这种情况下，如果还返回List集合，使用者就必须手动从集合中取出第一个元素。
  * 所以可以做一些优化设计，使得使用者更加方便
    * 无非就是在底层帮使用者完成了一个list.get(0)获取和判断

  ```java
  public List<T> selectList(){}
  public T selectOne(){}
  ```




## 11 动态数据库连接信息

* 使用者需要连接不同的数据库
* 所以就需要不同的连接信息，就不能将连接信息直接编写在程序中。
* **如何提供动态的连接信息呢？**
  1.  传参。
  2.  **配置信息**。
      * 一次编写，多次使用。
      * 所谓的配置，就是使用者和框架之间的一种沟通手段。





* **思考另外一个问题**

  * 这个配置信息是谁来提供的， 又是谁来使用这些配置信息的呢 ?

    ​				 **使用者**                                          **框架**

* **再思考下一个问题**

  * 使用者随便提供的配置信息，框架都可以使用么？  不可以。

  * 需要使用者按照一定的规则来提供配置信息，这个规则谁来提供呢？  **框架。**

  * 框架提供的规则主要包括： 在哪里提供配置信息，提供哪些配置信息，如何提供配置信息

    ​										                 **位置**                                **内容**                         **格式**

  1. 位置：暂时支持两个位置

     * 任意磁盘位置，只需要提供完整路径  ： f:/z/mysql.properties
     * classpath ( src )  , 只需要提供src下的相对路径  ： com/file/mysql.properties

  2. 内容：目前是4个内容   driver ， url ， username ， password

  3. 格式：要求使用properties文件记录配置信息 ， key的名称必须 driver , url , username , password

     ```properties
     driver=com.mysql.cj.jdbc.Driver
     url=jdbc:mysql://localhost:3306/duyi_orm
     username=root
     password=root
     ```



* **框架读取并使用配置信息**

  * 提供一个专门的对象来读取配置信息  **Configuration**

  * **注意：**

    * 虽然在一个程序中，绝大多数的情况下，都使用的是同一个数据库信息
    * 但也不排除，一个程序中，可以使用多个数据库信息
    * 所以有几份配置信息，就需要读取几份配置信息。
    * 所以这个配置信息的读取不能写在static代码段中。

    ```java
    /**
         * 支持在classpath目录中读取配置文件 <br/>
         * new Configuration("com/mysql-jdbc.properties");
         * @param classpathFile
    */
    public Configuration(String classpathFile){}
    
    /**
         * 支持任意路径下的配置文件<br/>
         * File file =new File("f:/z/oracle-jdbc.properties); <br/>
         * new Configuration(file)
         * @param file
    */
    public Configuration(File file){}
    ```

    

  * 框架如何使用配置信息呢？

    *  配置文件在哪里，使用者知道，所以使用者会创建Configuration对象，并提供配置文件路径
    *  所以框架要使用配置信息，使用者需要将configuration对象传递给框架

    ```java
    Configuration config= new Configuration("com/mysql-jdbc.properties") ;
    SqlSession session = new SqlSession(config);
    ```

    * Session 和 Executor都需要接收config对象

    ```java
    private Configuration configuration  ;
    public SqlSession(Configuration configuration){
        this(configuration,false);
    }
    
    public SqlSession(Configuration configuration , boolean isAutoCommit){
        this.configuration = configuration ;
        executor = new JdbcExecutor(configuration,isAutoCommit);
    }
    ```

    ```java
    private Configuration configuration ;
    public JdbcExecutor(Configuration configuration){
        this(configuration,false);
    }
    public JdbcExecutor(Configuration configuration , boolean isAutoCommit){}
    ```

    * 在executor中，数据库连接信息就都可以设置为动态了。
      * 其中关于驱动加载，不能再在static中加载了，需要在独立的方法调用加载。
      * 同时还要确保每一个数据库信息只加载一次

    ```java
    public JdbcExecutor(Configuration configuration , boolean isAutoCommit){
        this.configuration = configuration ;
        this.isAutoCommit = isAutoCommit ;
        //动态(信息)加载驱动
        if(!isDriver) {
            this.loadDriver();
        }
        conn = getConnection();
    }
    ```

    

    

## 12 SqlSessionFactory设计与实现

* 框架提供一个SqlSessionFactory对象

* 可以负责创建Configuration对象，创建SqlSession对象，并为Session传递config

  ```java
  /**
   * 负责创建configuration<br/>
   *      一个工厂可以创建多个SqlSession对象与数据库交互n次<br/>
   *      一个工厂创建的session应该是和一个数据库交互的<br/>
   *      如果需要和多个数据库交互，就应该产生多个工厂。<br/>
   *      一个工厂只需要读取一次配置信息 <br/>
   * 负责创建SqlSession<br/>
   *      每和数据库交互一次，都需要创建一个session (多实例）<br/>
   * 负责为session传递config
   */
  public class SqlSessionFactory {
      private Configuration configuration ;
  
      public SqlSessionFactory(String classpathFile){
          this.configuration = new Configuration(classpathFile) ;
      }
  
      public SqlSessionFactory(File file){
          this.configuration = new Configuration(file) ;
      }
  
  
      public SqlSession openSession(){
          return new SqlSession(configuration,false);
      }
  
      public SqlSession openSession(boolean isAutoCommit){
          return  new SqlSession(configuration,isAutoCommit) ;
      }
  
  }
  ```

* **问题1：一个工厂可以创建多个SqlSession，所以一个数据库连接信息只需要配一个工厂即可**

  * 工厂不能是单例模式。

  * 应该是一个数据库信息，只对应一个工厂对象。

  * 所以 **使用者应该提供一个封装**，确保一个数据库信息只对应一个工厂实例

    ```java
    public class SqlSessionFactoryUtil {
        //需要几个工厂，就造几个工厂
        public final static SqlSessionFactory mysqlFactory  ;
        public final static SqlSessionFactory oracleFactory ;
        static{
            mysqlFactory = new SqlSessionFactory("com/mysql-jdbc.properties");
    
            oracleFactory = new SqlSessionFactory(new File("f:/z/oracle-jdbc.properties"));
        }
    
    }
    ```

    ```java
    SqlSession session = SqlSessionFactoryUtil.mysqlFactory.openSession();
    ```

* **问题2：现在session改由工厂创建了，对于使用者而言，只需要知道工厂创建的session有哪些api即可**

  * 也就是说，使用者并不需要知道具体的session对象
  * 所以需要为session提供一个接口，实现使用者与框架对象之间的解耦。
    * 未来底层还可以更容易更换session对象实现。
    * 符合面向对象编程设计原则中的：**开闭原则**。

  ![1668311637254](imgs/01.png)



## 13 动态sql配置

### 13.1 分析

* 框架现在支持的都是新规则sql

* 目前我们的执行过程

  * dao调用session，传递sql
  * session调用executor，传递sql
  * executor先**处理sql**->处理参数->jdbc执行sql

* **我们分析发现**

  * 如果多次调用同一个dao方法，就会多次传递同一个sql，这一个sql就会被处理多次。
  * 每次处理sql，是需要花费一定的时间。
  * **我们就想**，既然每次处理的都是一样的sql，能不能只处理一次，下次发现还是上次的那个sql，就不处理，直接获得上一次的缓存结果。
  * **然后我们继续思考**，虽然dao不同的方法，需要不同的sql，但sql确定后，就不会再改变了。所以能不能再第一次执行之前就将其处理，执行时只需要找到这个处理后的sql就可以了。

  * 基于上面的两个思考， 我们将所有dao方法需要的sql都统一（配置）管理
  * 在框架启动时，就对这些sql进行一个处理
  * 当调用dao方法时，不在传递sql了，而是传递一个key，底层可以通过这个key找到之前已经处理好的sql

* **注意：**这里值提前处理sql（格式） ， 不能提前处理参数。





### 13.2 设计

* 首先，应该在哪里统一管理sql呢？

  1. 可以使用配置文件 。 但使用properties有些不足，使用其他配置文件又增加解析难度。
  2. **可以使用注解**

* 框架提供注解及规则，使用者通过注解管理sql，框架基于注解获得sql并处理

  * **框架提供Sql注解**，可以管理sql语句

    ```java
    public @interface Sql {
        String value();
    }
    ```

    

  * **使用者定义接口**，在接口的方法上使用注解管理sql

    ```java
    public interface CarSql {
        @Sql("insert into t_car values(null,#{cname},#{color},#{price})")
        public void save();
    }
    ```

  * 框架底层会使用 **类名+方法名**作为未来查找sql的key---"com.sql.CarSql.save"

  * **针对于insert时获得自增主键值**，需要为sql注解额外增加一个属性 propertyName

    ```java
    public @interface Sql {
        String value();
        /**
         * 在insert保存时，如果需要获得自增主键值，就通过该方法提供装载自增主键值的属性名
         * @return
         */
        String propertyName() default "" ;
    }
    ```

    ```java
    public interface CarSql {
        //告诉框架，执行该sql时，需要获得自增主键值，并将主键值存入参数的对象的cno属性中
        //相当于之前： session.insert(sql,car,"cno") ;
        @Sql(value="insert into t_car values(null,#{cname},#{color},#{price})",
             propertyName="cno")
        public void save();
    }
    ```

  * **针对于查询时的结果类型，由接口的返回类型决定**

    ```java
    public interface CarSql {
       
        //告诉框架，执行当前sql时，每条查询结果组成Car类型的对象。  Car是返回类型的泛型
        //相当于：session.selectList("...........................",Car.class);
        //现在执行时:session.selectList("com.sql.CarSql.findAll");
        @Sql("select * from t_car")
        public List<Car> findAll();
        
        //告诉框架，执行当前sql时，查询结果组成Car类型的对象。  Car是返回类型
        @Sql("select * from t_car where cno = 1")
        public Car findById();
    }
    ```

  * **针对于上面优化，返回类型只对查询有效， propertyName只对insert有效。所以可以提供CRUD注解**

    ```java
    public @interface Insert {}	//独有propertyName
    public @interface Update {}
    public @interface Delete {}
    public @interface Select {} //需要检查接口方法的返回类型（list的泛型）
    ```

    

### 13.3 实现

* 框架启动时，需要找到sql，并对其进行处理

* 使用者将sql写在了接口方法的注解中。

* 就需要告诉框架，sql在哪里。 通过配置文件告诉。现在配置文件需要加一个配置信息

  ```properties
  # 提供具体的管理sql的接口
  sql-mapper=com.sql.CarSql,com.sql.UserSql
  # 提供管理sql接口所在的包，会对包中的所有接口逐一处理
  sql-mapper-package=com.sql
  ```

* 在Configuration中读取配置信息时，就开始获得这些sql，并处理

  * 缓存的不是sql，而是处理sql的handler （包含sql）

  * 通过注解获得的propertyName，最终需要存储在handler中

  * 通过接口方法获得的返回类型，最终需要存储在handler中

  * 通过返回类型，获得rowType时，有可能返回类型就是rowType 。也可能返回类型List集合的泛型是rowType，需要通过反射获得泛型

    ```java
    Class<?> returnType = method.getReturnType();
    if(List.class.isAssignableFrom(returnType)){
        //返回类型是一个List集合，我们需要的是泛型
        //ParameterizedType表示带参数的类型，有泛型
        ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
        Class rowType = (Class) parameterizedType.getActualTypeArguments()[0];
        handler.setRowType(rowType);
    }else{
        //不是List集合。
        //目前我们只支持两种返回类型 ， domain ， List<domain>
        handler.setRowType(returnType);
    }
    ```

  * 需要为这种动态sql，提供一套新的session，executor方法。



## 14 代理实现dao

* 我们发现使用框架以后，每次数据库交互编码基本上就2行

  ```java
  org.orm.duyi.session.SqlSession session = 
      SqlSessionFactoryUtil.mysqlFactory.openSession(true);
  session.save("com.sql.CarSql.save",car);
  
  org.orm.duyi.session.SqlSession session = 
      SqlSessionFactoryUtil.mysqlFactory.openSession();
  List<Car> cars = session.searchList("com.sql.CarSql.findAll");
  ```

* 首先获得session的代码是固定的

* 其次每次执行的操作，可以由sql决定。

  * 通过session调用save方法，还是search方法，可以看看执行的sql是insert ，还是select
  * 而执行的sql，都在接口的方法上，只要想办法得到sql，就可以完成后续操作。

* 可以利用动态代理机制，为CarSql接口提供的代理对象

  * 每次调用代理对象时，就可以获得其代理的接口信息，从而获得sql语句，返回类型，自增属性

* **注意：**

  * 之前sql接口只负责管理sql ， 我们需要的是注解 + 返回类型
  * 现在sql接口还需要为动态代理提供模板，动态代理会根据接口生成dao的代理类。
    * 由于这个代理将dao的功能也实现了，所以代理类替代了dao
    * 也可以认为代理对象就是dao对象
  * 作为dao需要知道每次执行sql时的参数
  * 所以此时接口必须指定参数

![1668320300889](imgs/02.png)



* **注意：动态代理机制中Invocationhandler对象的作用及应用**

  ![1668321870473](imgs/03.png)

  * **invocationHandler.invoke方法中的3个参数**

    ```java
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args);
    ```

    * Object proxy 代表生成的代理对象 。 但这个代理对象尽量不要用，一用就死循环了。
    * Method method 代表当前调用的是代理的哪个方法，才进入invoke的
      * 可以通过这个method来实现目标方法的代用。 
      * 因为目标和代理有相同的方法。
    * Object[] args 代表调用代理方法时传递的参数， 而代理需要的参数最终是目标方法需要的 

  * **见案例22**

  * 

* **dao代理实现**

  ```java
  public <T> T getDaoProxy(Class<T> interfaceType) {
      final SqlSession currentSession = this ;
      //动态代理需要3个参数  ClassLoader ， interfaces , invocationHandler
      //动态代理执行时，会根据指定的接口产生代理类  （这个类以前没有，新产生的）
      //  新产生的类需要加载到jvm中才能使用， 利用类加载ClassLoader来加载。
      //      jdk提供了3个   bootstrap（jre） ， ext（ext.jar) , app（自定义）
      //类可以实现多个接口，所以需要提供接口数组（至少要有一个）
      //invocationhandler用来实现动态代理的调用功能。
      return (T) Proxy.newProxyInstance(
          Thread.currentThread().getContextClassLoader(),
          new Class[]{interfaceType},
          new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                  //正常来说，我们需要调用目标方法 。 目标是谁？ 应该是dao？
                  //后来我们发现，因为dao中的代码比较少，并且比较统一，所以代理就可以完成dao的操作
                  //  所以最终代理就代替了dao 。这个代理就可以理解成dao了。
                  //原来的dao做了什么事呢？
                  //  1 获得一个session 。
                  //      发现当前这个getDaoProxy这个方法，就是session对象的方法
                  //      这方法被执行了，说明session存在了
                  //      可以直接使用当前这个session
                  //  2 需要知道调用的是session的哪个方法，具体的sqlid是什么， 具体的参数是什么
                  //      参数就是代理得到的args参数
                  //      method可以方法名
                  //      interfaceType可知接口
                  //      接口名+"."+方法名 = sqlid
                  //      根据sqlid就可以到handler，就可以得到sql，就可以到curd语句
                  //SqlSession session = DefaultSqlSession.this ;
                  SqlSession session = currentSession ;
  
                  String sqlid = interfaceType.getName()+"."+method.getName();
                  SqlHandler handler = configuration.getHandler(sqlid);
                  String sqlOption = handler.getOldSql().substring(0, 6);
  
                  Object paramObj = null ;
                  if(args != null && args.length > 0){
                      //有参数
                      paramObj = args[0] ;
                  }
  
                  Object value = null ;
                  if("insert".equalsIgnoreCase(sqlOption)) {
                      value = session.save(sqlid,paramObj);
                  }else if("update".equalsIgnoreCase(sqlOption)){
                      value = session.modify(sqlid,paramObj);
                  }else if("delete".equalsIgnoreCase(sqlOption)){
                      value = session.remove(sqlid,paramObj) ;
                  }else if("select".equalsIgnoreCase(sqlOption)){
                      Class<?> returnType = method.getReturnType();
                      if(List.class.isAssignableFrom(returnType)){
                          value = session.searchList(sqlid,paramObj);
                      }else{
                          value = session.searchOne(sqlid,paramObj);
                      }
                  }
  
                  return value;
              }
          }
      );
  ```

  

# 最后

* 关联查询不支持
* 连接池不支持
