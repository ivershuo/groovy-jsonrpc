package groovy.jsonrpc.handler;

import static groovy.jsonrpc.engine.RpcRequest.newNotify;
import static groovy.jsonrpc.engine.RpcRequest.newRequst;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import groovy.jsonrpc.constant.Constant;
import groovy.jsonrpc.engine.RpcResponse.RpcRespError;
import groovy.jsonrpc.engine.RpcResponse.RpcRespResult;
import groovy.jsonrpc.handler.UrlHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class UrlHandlerTest {
    static final UrlHandler handler = new UrlHandler();

    static final String url = "test/test.groovy";

    static Object call(String url, Object reqobj, Class<?> clazz) {
	String req = JSON.toJSONString(reqobj);
	String rsp = call(url, req);
	return JSON.parseObject(rsp, clazz);
    }

    static Object call(String url, String req, Class<?> clazz) {
	String rsp = call(url, req);
	return JSON.parseObject(rsp, clazz);
    }

    static String call(String url, Object reqobj) {
	String req = JSON.toJSONString(reqobj);
	return call(url, req);
    }

    static List<RpcRespResult> callbatch(String url, Object[] reqobj) {
	String req = JSON.toJSONString(reqobj);
	String rsp = call(url, req);
	return JSON.parseArray(rsp, RpcRespResult.class);
    }

    static List<RpcRespError> callbatcherrors(String url, String req) {
	String rsp = call(url, req);
	return JSON.parseArray(rsp, RpcRespError.class);
    }

    static String call(String url, String req) {
	System.out.println(">" + req);
	String rsp = handler.call(url, req);
	System.out.println("<" + rsp);
	return rsp;
    }

    @Test
    public void testCallGroovy() {
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, "add", new int[] { 1, 2 }), RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(3, rsp.result);
	// 3 parameters
	rsp = (RpcRespResult) call(url,
		newRequst(1, "adds", new int[] { 1, 2, 3 }),
		RpcRespResult.class);
	assertEquals(1, rsp.id);
	assertEquals(6, rsp.result);
    }

    @Test
    public void testCallArgsCount() {
	// no parameter
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, "getDate"), RpcRespResult.class);
	assertNull(rsp.id);
	assertNotNull(rsp.result);
	// 1 parameter
	rsp = (RpcRespResult) call(url, newRequst(null, "fun1arg", 1),
		RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(2, rsp.result);
	// 2 parameters
	rsp = (RpcRespResult) call(url,
		newRequst(null, "add", new int[] { 1, 2 }), RpcRespResult.class);
	assertNull(rsp.id);
	assertEquals(3, rsp.result);
	// 3 parameters
	rsp = (RpcRespResult) call(url,
		newRequst(1, "adds", new int[] { 1, 2, 3 }),
		RpcRespResult.class);
	assertEquals(1, rsp.id);
	assertEquals(6, rsp.result);
    }

    @Test
    public void testCallAppException() {
	RpcRespError rsp = (RpcRespError) call(url,
		newRequst(null, "adds", new int[] { 1, 2 }), RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_DEFAULT_APP_ERROR, rsp.error.code);
	assertNotNull(rsp.error.message);
	assertNotNull(rsp.error.data);
    }

    @Test
    public void testCallMethodNotFound() {
	RpcRespError rsp = (RpcRespError) call(url,
		newRequst(null, "notfound", new int[] { 1, 2 }),
		RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_METHOD_NOT_FOUND, rsp.error.code);
	assertNotNull(rsp.error.message);
	assertNull(rsp.error.data);
    }

    @Test
    public void testCallMethodParamsError() {
	RpcRespError rsp = (RpcRespError) call(url, newRequst(null, "add", 1),
		RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_PARAMS, rsp.error.code);
	assertNotNull(rsp.error.message);
	assertNotNull(rsp.error.data);
    }

    @Test
    public void testCallBatchBase() {
	Object[] batch = new Object[] {
		newRequst(null, "add", new int[] { 1, 2 }),
		newRequst(2, "adds", new int[] { 1, 2, 3 }) };
	List<RpcRespResult> rsp = callbatch(url, batch);
	assertEquals(2, rsp.size());
	RpcRespResult rsp1 = rsp.get(0);
	assertNull(rsp1.id);
	assertEquals(3, rsp1.result);
	RpcRespResult rsp2 = rsp.get(1);
	assertEquals(2, rsp2.id);
	assertEquals(6, rsp2.result);
    }

    @Test
    public void testCallBatchContainsNotify() {
	Object[] batch = new Object[] {
		newRequst(1, "add", new int[] { 1, 2 }), newNotify("donotify") };
	List<RpcRespResult> rsp = callbatch(url, batch);
	assertEquals(1, rsp.size());
	RpcRespResult rsp1 = rsp.get(0);
	assertEquals(1, rsp1.id);
	assertEquals(3, rsp1.result);
    }

    @Test
    public void testCallBatchAllNotify() {
	Object[] batch = new Object[] { newNotify("donotify"),
		newNotify("notfoundnotify") };
	String rsp = call(url, batch);
	assertEquals("", rsp);
    }

    @Test
    public void testCallBase() {
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, "rpc.ls"), RpcRespResult.class);
	assertEquals(Constant.VERSION, rsp.jsonrpc);
	assertNull(rsp.id);
	assertNotNull(rsp.result);
    }

    @Test
    public void testCallRpccmd() {
	String[] cmds = new String[] { "rpc.ls", "rpc.ll", "rpc.all",
		"rpc.recompile" };
	for (String cmd : cmds) {
	    RpcRespResult rsp = (RpcRespResult) call(url, newRequst(null, cmd),
		    RpcRespResult.class);
	    assertEquals(Constant.VERSION, rsp.jsonrpc);
	    assertNull(rsp.id);
	    assertNotNull(rsp.result);
	}
    }

    @Test
    public void testCallAutoConvertParameter() {
	// 1 arg
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, "fun1arg", new int[] { 1 }),
		RpcRespResult.class);
	assertEquals(2, rsp.result);
	rsp = (RpcRespResult) call(url, newRequst(null, "fun1arg", 1),
		RpcRespResult.class);
	assertEquals(2, rsp.result);
	// 1 arg String
	rsp = (RpcRespResult) call(url,
		newRequst(null, "fun1argstr", new String[] { "abc" }),
		RpcRespResult.class);
	assertEquals("cba", rsp.result);
	// 2 args
	rsp = (RpcRespResult) call(url,
		newRequst(null, "add", new int[] { 1, 2 }), RpcRespResult.class);
	assertEquals(3, rsp.result);
    }

    @Test
    public void testCallNotify() {
	String rsp = call(url, newNotify("donotify"));
	assertEquals("", rsp);
	rsp = call(url, newNotify("echo", new int[] { 1, 2 }));
	assertEquals("", rsp);
    }

    @Test
    public void testCallCheckId() {
	// null
	RpcRespResult rsp = (RpcRespResult) call(url,
		newRequst(null, "rpc.ls"), RpcRespResult.class);
	assertNull(rsp.id);
	// int
	rsp = (RpcRespResult) call(url, newRequst(1, "rpc.ls"),
		RpcRespResult.class);
	assertEquals(1, rsp.id);
	rsp = (RpcRespResult) call(url, newRequst("strid", "rpc.ls"),
		RpcRespResult.class);
	// long
	assertEquals(rsp.id, "strid");
	long id = Long.MAX_VALUE;
	rsp = (RpcRespResult) call(url, newRequst(id, "rpc.ls"),
		RpcRespResult.class);
	assertEquals(id, rsp.id);
	id = Long.MIN_VALUE;
	rsp = (RpcRespResult) call(url, newRequst(id, "rpc.ls"),
		RpcRespResult.class);
	assertEquals(id, rsp.id);
	// complicate object
	Map<String, String> mid = new HashMap<String, String>();
	mid.put("aa", "bb");
	rsp = (RpcRespResult) call(url, newRequst(mid, "rpc.ls"),
		RpcRespResult.class);
	assertTrue(rsp.id instanceof Map);
	@SuppressWarnings("unchecked")
	Map<String, String> rspmid = (Map<String, String>) (rsp.id);
	assertEquals("bb", rspmid.get("aa"));
    }

    @Test
    public void testCallInValid() {
	// parse error
	RpcRespError rsp = (RpcRespError) call(url, "[", RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_PARSE_ERROR, rsp.error.code);
	// parse error, not terminated batch
	rsp = (RpcRespError) call(
		url,
		"[ {'jsonrpc': '2.0', 'method': 'sum'},  {'jsonrpc': '2.0', 'method']",
		RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_PARSE_ERROR, rsp.error.code);
	// empty batch
	rsp = (RpcRespError) call(url, "[]", RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.error.code);
	// rpc call with an invalid Batch (but not empty)
	List<RpcRespError> rspes = callbatcherrors(url, "[1]");
	assertEquals(1, rspes.size());
	rsp = rspes.get(0);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.error.code);
	// rpc call with invalid Batchs
	rspes = callbatcherrors(url, "[1, 2, 3]");
	assertEquals(3, rspes.size());
	rsp = rspes.get(0);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.error.code);
	// jsonrpc miss
	rsp = (RpcRespError) call(url, "{}", RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.error.code);
	// method miss
	rsp = (RpcRespError) call(url, "{jsonrpc:'2.0'}", RpcRespError.class);
	assertNull(rsp.id);
	assertEquals(Constant.EC_INVALID_REQUEST, rsp.error.code);
    }

    @Test
    public void testRegister() {
	handler.initbase("test/testbase.groovy", "test/testsub.groovy");
    }

    @Test
    public void testFailUrl() {
	RpcRespError rsp = (RpcRespError) call("failurl",
		newRequst(1, "rpc.ls"), RpcRespError.class);
	assertEquals(1, rsp.getId());
	assertEquals(Constant.EC_INTERNAL_ERROR, rsp.error.code);
    }
}
