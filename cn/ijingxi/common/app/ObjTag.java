
package cn.ijingxi.common.app;

import java.util.Date;
import java.util.Queue;

import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.jxCompare;

public class ObjTag extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(ObjTag.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(ObjTag.class);
	}

	@ORM(keyType=KeyType.AutoDBGenerated)
	public int ID;
	
	@ORM(Index=1,Descr="对象一定都是容器")
	public int ObjID;

	@ORM(Index=1,Encrypted=true)
	public int TagID;
	
	@ORM
	public String Descr;
	
	@ORM(Descr="标记是可以带有状态的")
	public int TagState;
	
	@ORM(Index=2,Descr="标记时的时间")
	public Date TagTime;	

	@ORM
	public float Number;	

	@ORM(Index=3,Descr="时间点信息，如todo的发生时间，两个时间点Tag可以组成时间段")
	public Date Time;

	//@ORM(Index=3,Descr="时间段信息，和Time组合使用，Time是起点，如日程安排")
	//public Date ToTime;
	
	@ORM(Descr="json格式的附加信息",Encrypted=true)
	public String Addition;

	public Queue<jxORMobj> List(int id,String TagName) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag");
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, id);
		s.AddContion("ObjTag", "TagName", jxCompare.Equal, TagName);
		return Select(ObjTag.class,s);
	}
	public ObjTag Get(int id,String TagName) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("ObjTag");
		s.AddContion("ObjTag", "ObjID", jxCompare.Equal, id);
		s.AddContion("ObjTag", "TagName", jxCompare.Equal, TagName);
		return (ObjTag) Get(ObjTag.class,s);
	}
	
}

