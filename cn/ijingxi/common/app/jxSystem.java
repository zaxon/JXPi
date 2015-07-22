
package cn.ijingxi.common.app;

import cn.ijingxi.common.Process.IExecutor;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.SelectSql;
import cn.ijingxi.common.orm.jxORMobj;
import cn.ijingxi.common.util.Trans;

import java.util.*;

/**
 * 全局的，所有topspace共享
 * @author andrew
 *
 */
public class jxSystem extends jxORMobj
{	
	public static final UUID zeroUUID=UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static final UUID broadUUID=UUID.fromString("11111111-1111-1111-1111-111111111111");

	public static jxSystem System=null;
	public static UUID SystemID=null;
	public static void Init() throws Exception
	{	
		InitClass(ORMType.Container.ordinal(),jxSystem.class);
		try
		{
			SelectSql s=new SelectSql();
			s.AddTable("jxSystem",null);
			System=(jxSystem) Get(jxSystem.class,s,null);	
			SystemID=System.ID;
		} catch (Exception e) {}
	}	

	@Override
	protected void Init_Create() throws Exception
	{
		ID=UUID.randomUUID();
	}
	
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(jxSystem.class,null);
		System=(jxSystem) jxORMobj.Create(jxSystem.class);
		System.Insert(null);
		SystemID=System.ID;
	}
	
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;
	
	
	//用{}框起来的字符串Purpose（流水号用在哪，如流程名）、Name（当前请求者的姓名）YYYY、MM、DD或YYYYMMDD、SND4（4位日流水）、
	//SNT8（8位总流水）之类
	//如：{Purpose}-{Name}-{YYYYMMDD}-{SND3}，将产生：报销-徐晓轶-20150120-003
	@ORM(Descr="json格式的流水号模板")
	public String SN;

	@ORM(Descr="json格式的附加信息")
	public String Info;
		
	private static Map<String,SN> SNTree=new HashMap<String,SN>();
		
	public String GetSN(String Purpose,IExecutor caller) throws Exception
	{
		SN sn=SNTree.get(Purpose);
		if(sn==null)
		{
			Map<String,String> ks=new HashMap<String,String>();
			ks.put("Purpose", Purpose);
			String m=getExtendArrayValue("SN",ks,"Model");
			if(m==null)
				throw new Exception(Purpose+"未设置流水号！！");
			String dn=getExtendArrayValue("SN",ks,"DayNumber");
			String tn=getExtendArrayValue("SN",ks,"TotalNumber");
			String dt=getExtendArrayValue("SN",ks,"LastDate");
			sn=new SN(this,Purpose,m,Trans.TransToDate(dt));
			sn.Number=Trans.TransToInteger(dn);
			sn.TatolNumber=Trans.TransToInteger(tn);
			SNTree.put(Purpose, sn);
		}
		return sn.GetNumber(caller);
	}

	public int GetAutoGeneratedID(String ClsName) throws Exception
	{
		synchronized (this)
		{
			int num=Trans.TransToInteger(getExtendValue("Info",ClsName));
			num++;
			setExtendValue("Info",ClsName,num);
			Update(null);
			return num;
		}
	}
	
	void SaveSN_LastDate(String Purpose,Date d) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("Purpose", Purpose);
		setExtendArrayValue("SN",ks,"LastDate",Trans.TransToString(d));
	}
	void SaveSN_TotalNumber(String Purpose,int d) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("Purpose", Purpose);
		setExtendArrayValue("SN",ks,"TotalNumber",d);
	}
	void SaveSN_DayNumber(String Purpose,int d) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("Purpose", Purpose);
		setExtendArrayValue("SN",ks,"DayNumber",d);
	}
	public void AddSNModel(String Purpose,String Model) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("Purpose", Purpose);
		setExtendArrayValue("SN",ks,Purpose,Model);
	}
	
}
