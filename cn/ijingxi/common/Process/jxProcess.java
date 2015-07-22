
package cn.ijingxi.common.Process;

import cn.ijingxi.common.app.*;
import cn.ijingxi.common.orm.*;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxCompare;
import cn.ijingxi.common.util.jxOP;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 流程的数据存储，其实就是流程的模板
 * @author andrew
 *
 */
public class jxProcess extends Container
{
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.jxProcess.ordinal(),ID);
	}
	
	public static void Init() throws Exception{	InitClass(ORMType.jxProcess.ordinal(),jxProcess.class);}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(jxProcess.class,ts);
	}

	@Override
	protected void Init_Create() throws Exception
	{
		ID=UUID.randomUUID();
	}
	/*
	protected boolean DualEventMsg(jxMsg msg) throws Exception
	{
		IjxEnum event = msg.getEvent();
		if(event==null||!(event instanceof InstanceEvent))return false;
		TopSpace ts=msg.getTopSpace();
		if(ts==null)return false;
		WorkNode from=(WorkNode) jxORMobj.GetFromJSONString(msg.GetParam("Node"));
		String toname=msg.GetParam("To");
		switch((InstanceEvent)event)
		{
		case Touch:
			WorkNode WorkNode=getPN(ts,from.ParentID,toname);
			if(WorkNode==null)
			{
				WorkNode=(WorkNode) WorkNode.Create(WorkNode.class);
				WorkNode.ParentOwnerID=msg.Receiver;
				WorkNode.ParentID=from.ParentID;
				WorkNode.ProcessID=from.ProcessID;
				WorkNode.Parent=from.Parent;
				WorkNode.Name=toname;
				WorkNode.Insert(ts);
			}
			WorkNode.Touch(ts, msg.getMsg());
				
			
			
			return true;
		default:
			break;
		}
		return false;
	
	
	
	}
*/	
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;

	@ORM(Descr="流水号模板名称")
	public String SNPurpose; 
	
	@ORM(Descr="版本号，用于同名进程模板之间的区分")
	public String Version; 
	static Pattern PatternVersion = Pattern.compile("^[1-9]\\d+\\.\\d+\\.\\d+\\.\\d+$");
		
	@ORM(Descr="json格式的所有节点名")
	public String Nodes;		

	@ORM(Descr="json格式的所有流转，自动执行条件，是绑定在各条trans上的，只有设为auto的才可以")
	public String NodeTrans;

	@ORM(Descr="用于显示本流程的处理页面，这样当新的流程处理方法出来后原流程可不受影响")
	public String DispURL;		
	
	@ORM
	public Boolean NoUsed;
		
	@Override
	protected void Verify() throws Exception
	{
		if(Version!=null&&!PatternVersion.matcher(Version).matches())
			throw new Exception("版本号的格式应为：vvv.vvv.vvv.vvv");		
	}
	
	//发起新流程时调用
	public PI StartNewInstance(TopSpace ts, PeopleInTs Caller,String Msg) throws Exception
	{
		PI p=(PI) Create(PI.class);
		//p.ParentOwnerID=Caller.ID;
		p.Name=jxSystem.System.GetSN(SNPurpose,Caller);
		if(p.Name==null)
			p.Name=Name;
		p.ProcessID=this.ID;
		p.Descr=Msg;
		p.Insert(ts);
		//启动流程
		WorkNode sn=(WorkNode) Create(WorkNode.class);
		//sn.setParam(Caller,ID,p, WorkNode.Node_Start);		
		sn.Touch(ts, null);
		return p;
	}
	
	public WorkNode getPN(TopSpace ts, UUID PIID, String PNName) throws Exception
	{
		SelectSql s=new SelectSql();
		s.AddTable("WorkNode",ts);
		s.AddContion("WorkNode", "ParentID", jxCompare.Equal, PIID);
		s.AddContion("WorkNode", "Name", jxCompare.Equal, PNName);
		return (WorkNode) Get(WorkNode.class,s,ts);
	}
	/**
	 * 修改也是这个函数
	 * @param NodeName
	 * @param InputTypeIsAnd
	 * @param OutputTypeIsAnd
	 * @param Auto
	 * @param AutoByExecer 在自动执行是是否对后继分支检查有执行者即执行
	 * @param ExecerID
	 * @throws Exception
	 */
	public void setNode(String NodeName,boolean InputTypeIsAnd,boolean OutputTypeIsAnd,boolean Auto,UUID ExecerID) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		setExtendArrayValue("Nodes",ks,"InputType",InputTypeIsAnd);
		setExtendArrayValue("Nodes",ks,"OutputType",OutputTypeIsAnd);
		setExtendArrayValue("Nodes",ks,"Auto",Auto);
		setExtendArrayValue("Nodes",ks,"Execer",ExecerID);
	}
	public List<String> ListNodes() throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		List<jxJson> ls=getExtendArrayList(Nodes,ks);
		List<String> rs=new LinkedList<String>();
		for(jxJson js:ls)
			rs.add((String) js.GetSubValue("NodeName"));
		return rs;
	}
	public boolean getNode_InputType(String NodeName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		String rs=getExtendArrayValue("Nodes",ks,"InputType");
		if(rs!=null)
			return Trans.TransToBoolean(rs);
		return false;
	}
	public void setNode_InputType(String NodeName,boolean InputTypeIsAnd) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		setExtendArrayValue("Nodes",ks,"InputType",InputTypeIsAnd);
	}
	public boolean getNode_OutputType(String NodeName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		String rs=getExtendArrayValue("Nodes",ks,"OutputType");
		if(rs!=null)
			return Trans.TransToBoolean(rs);
		return false;
	}
	public void setNode_OutputType(String NodeName,boolean OutputTypeIsAnd) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		setExtendArrayValue("Nodes",ks,"OutputType",OutputTypeIsAnd);
	}
	public boolean getNode_Auto(String NodeName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		String rs=getExtendArrayValue("Nodes",ks,"Auto");
		if(rs!=null)
			return Trans.TransToBoolean(rs);
		return false;
	}
	public void setNode_Auto(String NodeName,boolean Auto) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		setExtendArrayValue("Nodes",ks,"Auto",Auto);
	}
	public boolean getNode_AutoByExecer(String NodeName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		String rs=getExtendArrayValue("Nodes",ks,"AutoByExecer");
		if(rs!=null)
			return Trans.TransToBoolean(rs);
		return false;
	}
	public void setNode_AutoByExecer(String NodeName,boolean AutoByExecer) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		setExtendArrayValue("Nodes",ks,"AutoByExecer",AutoByExecer);
	}
	/*

	public Result getNode_AutoResult(String NodeName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		String rs=getExtendArrayValue(Nodes,ks,"Result");
		if(rs!=null)
			return (Result) Trans.TransTojxEunm("Result", Trans.TransToInteger(rs));
		return Result.None;
	}
	public void setNode_AutoResult(String NodeName,Result result) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		setExtendArrayValue(Nodes,ks,"Result",result.ordinal());
	}

	*/
	public PeopleInTs getNode_RealExecer(TopSpace ts,String NodeName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		jxJson rs=getExtendArrayNode("Nodes",ks);
		if(rs==null)return null;
		ORMID id=ORMID.GetFromJSON(rs.GetSubObject("ExecerID"));
		if(id==null)return null;
		if(id.getTypeID()==ORMType.People.ordinal())
			return (PeopleInTs) GetByID(PeopleInTs.class,id.getID(),null);
		if(id.getTypeID()==ORMType.Role.ordinal())
		{
			Role r=(Role) GetByID(Role.class,id.getID(),null);
			if(r!=null)
				return r.GetMapTo(ts);
		}
		return null;
	}
	public void setNode_ExecerID(String NodeName,ORMID ExecerID) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("NodeName", NodeName);
		setExtendArraySubNode("Nodes",ks,ExecerID.ToJSON("ExecerID"));
	}
	Organize myOrg=null;
	public Organize GetOrganize(TopSpace ts) throws Exception
	{
		if(myOrg!=null)return myOrg;
		SelectSql s=new SelectSql();
		s.AddTable("Relation",ts);
		s.AddTable("Organize",ts);
		s.AddContion("Relation", "ObjTypeID", jxCompare.Equal, ORMType.Organize.ordinal());
		s.AddContion("Relation", "ObjID", "Organize","ID");
		s.AddContion("Relation", "TargetTypeID", jxCompare.Equal, ORMType.jxProcess.ordinal());
		s.AddContion("Relation", "TargetID", jxCompare.Equal, ID);
		myOrg=(Organize) Get(jxProcess.class,s,ts);
		return myOrg;
	}
	
	
		
	public void setTrans(String From,String ExportName,String To) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		ks.put("Export", ExportName);
		setExtendArrayValue("NodeTrans",ks,"To",To);
	}
	public String getTrans_To(String From,String ExportName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		ks.put("Export", ExportName);
		return getExtendArrayValue("NodeTrans",ks,"To");
	}		
	public String getTrans_Export(String From,String To) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		ks.put("To", To);
		return getExtendArrayValue("NodeTrans",ks,"Export");
	}	
	public List<String> ListFrom(String To) throws Exception
	{
		List<String> rs=new LinkedList<String>();
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("To", To);
		List<jxJson> jl=getExtendArrayList("NodeTrans",ks);
		if(jl.size()>0)
			for(jxJson j:jl)
				rs.add((String) j.getSubObjectValue("From"));
		return rs;
	}
	public List<String> ListExport(String From) throws Exception
	{
		List<String> rs=new LinkedList<String>();
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		List<jxJson> jl=getExtendArrayList("NodeTrans",ks);
		if(jl.size()==0)return null;
		for(jxJson j:jl)
			rs.add((String) j.getSubObjectValue("To"));
		return rs;
	}

		
	public void setTranContion(String From,String ExportName,ContionLink cl) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		ks.put("Export", ExportName);
		setExtendArraySubNode("NodeTrans",ks,cl.TojxJson());
	}	
	public ContionLink getTranContion(String From,String ExportName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		ks.put("Export", ExportName);
		List<jxJson> rs=getExtendArrayList("NodeTrans",ks);
		if(rs!=null&&rs.size()==1)
			return new ContionLink(rs.get(0).GetSubObject("ContionLink"));
		return null;
	}	
	public void setNodeOP(String NodeName,OPLink ol) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("Node", NodeName);
		setExtendArraySubNode("Nodes",ks,ol.TojxJson());
	}	
	public OPLink getNodeOP(String NodeName) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("Node", NodeName);
		List<jxJson> rs=getExtendArrayList("Nodes",ks);
		if(rs!=null&&rs.size()==1)
			return new OPLink(rs.get(0).GetSubObject("OPLink"));
		return null;
	}	


	
	/**
	 * 将进程初始化为G5流程，这个只需要在创建流程时用一次就可以了，不必每次都初始化
	 * @throws Exception
	 */
    public void G5_Init() throws Exception
    {
    	setNode(WorkNode.Node_Start,false,false,true,null);
    	setNode_AutoByExecer(WorkNode.Node_Start,true);
    	setNode(WorkNode.Node_End,false,false,true,null);
    	setNode_AutoByExecer(WorkNode.Node_End,true);
    	
    	setNode(WorkNode.Node_Accept,false,false,true,null);
    	setNode_AutoByExecer(WorkNode.Node_Accept,true);
    	//setNode_AutoResult(WorkNode.Node_Accept,Result.Accept);
    	OPLink ol=new OPLink();
    	ol.AddOP("Result", null, jxOP.Equal, Result.Accept);
    	setNodeOP(WorkNode.Node_Accept,ol);
    	
    	setNode(WorkNode.Node_Reject,false,false,true,null);
    	setNode_AutoByExecer(WorkNode.Node_Reject,true);
    	//setNode_AutoResult(WorkNode.Node_Reject,Result.Reject);
    	ol=new OPLink();
    	ol.AddOP("Result", null, jxOP.Equal, Result.Reject);
    	setNodeOP(WorkNode.Node_Reject,ol);
    	
    	setNode("申请",false,false,false,null);
    	setNode("第一级审核",false,false,false,null);
    	setNode("第一级已同意",false,false,true,null);
    	setNode_AutoByExecer("第一级已同意",true);
    	setNode("第二级审核",false,false,false,null);
    	setNode("第二级已同意",false,false,true,null);
    	setNode_AutoByExecer("第二级已同意",true);
    	setNode("第三级审核",false,false,false,null);
    	setNode("第三级已同意",false,false,true,null);
    	setNode_AutoByExecer("第三级已同意",true);
    	setNode("第四级审核",false,false,false,null);
    	setNode("第四级已同意",false,false,true,null);
    	setNode_AutoByExecer("第四级已同意",true);
    	setNode("第五级审核",false,false,false,null);

    	setTrans("自动",WorkNode.Node_Start,"申请");
    	setTrans("申请","申请","第一级审核");
    	setTrans("拒绝","第一级审核",WorkNode.Node_Reject);
    	setTrans("同意","第一级审核","第一级已同意");
    	setTrans("后继节点有人执行","第一级已同意","第二级审核");
    	setTrans("后继节点无人执行","第一级已同意",WorkNode.Node_Accept);
    	setTrans("拒绝","第二级审核",WorkNode.Node_Reject);
    	setTrans("同意","第二级审核","第二级已同意");
    	setTrans("后继节点有人执行","第二级已同意","第三级审核");
    	setTrans("后继节点无人执行","第二级已同意",WorkNode.Node_Accept);
    	setTrans("拒绝","第三级审核",WorkNode.Node_Reject);
    	setTrans("同意","第三级审核","第三级已同意");
    	setTrans("后继节点有人执行","第三级已同意","第四级审核");
    	setTrans("后继节点无人执行","第三级已同意",WorkNode.Node_Accept);
    	setTrans("拒绝","第四级审核",WorkNode.Node_Reject);
    	setTrans("同意","第四级审核","第四级已同意");
    	setTrans("后继节点有人执行","第四级已同意","第五级审核");
    	setTrans("后继节点无人执行","第四级已同意",WorkNode.Node_Accept);
    	setTrans("拒绝","第五级审核",WorkNode.Node_Reject);
    	setTrans("同意","第五级审核",WorkNode.Node_Accept);
    	
    	setTrans("自动",WorkNode.Node_Reject,WorkNode.Node_End);
    	setTrans("自动",WorkNode.Node_Accept,WorkNode.Node_End);    	
    }
    

}