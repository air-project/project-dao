# project-dao
common dao/service base on hibernate4/spring4
 
 
maven

	<dependency>
	  <groupId>com.github.air-project</groupId>
	  <artifactId>project-dao</artifactId>
	  <version>0.1</version>
	</dependency>

1. <context:component-scan base-package="com.thinkgem.jeesite,com.air"><!-- base-package如果多个，用“,”分隔 -->

2. <!-- 定义Hibernate Session工厂 -->
	<bean id="sessionFactory" class="org.springframework.orm.hibernate4.LocalSessionFactoryBean"> 
		<property name="dataSource" ref="dataSource"/>
		<property name="packagesToScan" value="com.air"/><!-- 如果多个，用“,”分隔 -->
		
3. 你也可以拥有自己的service,继承CommonServiceImpl
public class UserServiceImpl extends CommonServiceImpl implements UserService
这里您可以直接使用commonDao(除非有必要)，建议使用用CommonServiceImpl已有的基本方法

其中public <T> List<T> list(Class<T> clazz, T t, Page page, Order... orders);
自动完成查询条件封装，及分页等

4.测试类：

@Entity
@Table(name="my_user_test")
@Data
@EqualsAndHashCode(callSuper=false)
public class UserEntity extends BaseEntity {

}


public class BaseDaoTest extends SpringTransactionalContextTests {
	
	@Autowired
	private CommonDao commonDao;
	
	@Autowired
	private CommonService commonService;

//	@Test
	public void save(){
		UserEntity u = new UserEntity();
		u.setCreateUser("aaaaaaa");
		u.setName("466666666666");
		u.setTestDate(new Date());
		UserEntity u1 = new UserEntity();
		u1.setCreateUser("aaaaaaa");
		u1.setName("55555555555");
		u1.setTestDate(new Date());
		List<UserEntity> list = Lists.newArrayList();
		list.add(u1);
		list.add(u);
		commonService.saveAll(list);
	}
	
//	@Test
	public void get(){
		System.out.println(commonDao.get(UserEntity.class, 7L));
	}
	
//	@Test
	public void update(){
		UserEntity u=commonDao.get(UserEntity.class, 7L);
		u.setName("wo woo woooooo");
		commonService.update(u);
		System.out.println(commonService.get(UserEntity.class, 7L));
	}
	
	@Test
	public void find() throws ParseException{
		UserEntity u = new UserEntity();
//		u.setName("test153");//根据Name查询
		QueryDateRange range=new QueryDateRange();
//		range.idsRange("name",RangeType.STRING,"2,3");//查询某个时间范围
		range.idsRange("id",RangeType.LONG,"1,4,5");//查询某个状态范围
		u.setDateRange(range);
		
		List<UserEntity> list=commonService.list(UserEntity.class, u, null);
		list.forEach(uu->System.out.println(uu));
		
	}
	
	@Test
	public void page() throws ParseException{
		UserEntity u = new UserEntity();
		Page page = new Page();
		page.setPageSize(2);
		List<UserEntity> list=commonService.list(UserEntity.class, u, page);
		System.out.println(page.getTotalCount()+"--->"+page.getTotalPage());
		
	}
}		
