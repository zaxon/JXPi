
package cn.ijingxi.common.app;

import java.util.*;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;

public class People extends Container
{
	protected People()
	{
		super();
		TypeName="People";
		ContainerType=ContainerType_People;
	}
	
	public static ORMID GetORMID(Integer ID)
	{
		return new ORMID(GetTypeID("People"),ID);
	}
	
	/**
	 * 要在Container之后执行
	 * @throws Exception
	 */
	public static void Init() throws Exception{	InitClass(People.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(People.class);
	}
	@Override
	protected boolean CheckForMsgRegister() throws Exception
	{
		//如果是在自己的手机上则接收消息
		return jxSystem.System.SystemUUID.compareTo(UniqueID)==0;
	}
	
	
	//1号是手机主人，但如果某人在两台手机上都装了，则会出现冲突，需要加以解决
	@ORM(keyType=KeyType.PrimaryKey)
	public int ID;

	@ORM(Index=1)
	public String LoginName;

	@ORM(Index=2)
	public Date Birthday;
			
	@ORM
	public String Passwd;	
	
	@ORM(Descr="json格式的安全问题与答案：Question、Answer")
	public String Secure;
	
	@ORM(Descr="json格式的联系方式，包括但不限于Mail、Tel、Mobile、Fax、Address、国家等等")
	public String Contact;
		
	@ORM
	public Boolean NoUsed;

	@Override
	public boolean CheckRight(Container c,String RoleName) throws Exception
	{
		Queue<jxORMobj> pl=((Organize)c).ListRealMapTo(RoleName);
		if(pl!=null)
			for(jxORMobj p:pl)
				if(((People)p).GetID().Equal(GetID()))
					return true;
		return false;
	}

	
}