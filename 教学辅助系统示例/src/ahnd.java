import bll.*;
import cn.ijingxi.Rest.httpServer.jxHttpRes;
import cn.ijingxi.Rest.httpServer.jxHttpServer;
import cn.ijingxi.app.ObjTag;
import cn.ijingxi.communication.VirtualTCPDev.server;
import cn.ijingxi.communication.VirtualTCPDev.vDevJSON;
import cn.ijingxi.data.JdbcSqlite.JdbcSqlite;
import cn.ijingxi.orm.JdbcUtils;
import cn.ijingxi.orm.jxJson;
import cn.ijingxi.ui.system;
import cn.ijingxi.util.jxLog;
import cn.ijingxi.util.utils;
import org.luaj.vm2.LuaValue;

import static cn.ijingxi.app.ObjTag.getSystemLuaConf;

/**
 * Created by andrew on 16-5-24.
 */
public class ahnd {

    public static void main(String args[]) throws Exception {

        if(ObjTag.SystemID==null){
            //尚未初始化
            jxLog.logger.debug("Init");
            JdbcSqlite db=null;
            try {
                db = new JdbcSqlite("main");
                JdbcUtils.SetDB(db);

                utils.Init();
                common.Init();
                //if(ObjTag.System==null)
                {
                    //系统尚未建库
                    //jxLog.logger.debug("CreateDBTable");
                    utils.CreateDBTable();
                    common.CreateDBTable();
                }

                LuaValue v = getSystemLuaConf("WebServerPort");
                if (v.isnil()){
                    jxLog.logger.error("WebServerPort未指定，程序结束");
                    return;
                }

                //启动虚拟设备（如安卓手机）
                int sport=ObjTag.getvDepServerPort();
                if(sport>0){
                    server s=new server();
                    s.logged=true;

                    //自定义的扩展命令
                    s.dualCmd= p -> {
                        String cmd = vDevJSON.getCmd(p);
                        jxJson js=null;
                        switch (cmd) {

                            case "tt":
                                js = vDevJSON.getCmd_response(p);
                                s.send_json(js);
                                break;
                        }
                    };
                    s.startServer(sport);
                }


                jxHttpRes.InitResClass(system.class);

                jxHttpRes.InitResClass(Person.class);
                jxHttpRes.InitResClass(plan.class);
                jxHttpRes.InitResClass(schedule.class);
                jxHttpRes.InitResClass(question.class);
                jxHttpRes.InitResClass(team.class);

                jxHttpRes.InitResClass(task.class);
                jxHttpRes.InitResClass(coding.class);
                jxHttpRes.InitResClass(testing.class);
                jxHttpRes.InitResClass(subject.class);

                //jxHttpServer server1=new jxHttpServer(10000, "./web_visit/", null);
                //server1.start();
                //jxHttpServer server2=new jxHttpServer(10001, "./web_record/", null);
                //server2.start();
                jxHttpServer server3=new jxHttpServer(v.checkint(), "./web_manager/", null);
                server3.start();

            } catch (Exception e) {
                jxLog.error(e);
            }

        }
    }

}
