package com.conference.websocket;

import com.conference.bo.Message;
import com.conference.bo.UserInfo;
import com.conference.service.IceService;
import com.conference.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/video/conference/websocket/{sid}")
@Component
public class RtcHandler {

    private static String[] nameList;

    static {
        String names = "拔奇、鲍出、鲍信、鲍勋、毕×、邴原、卜静、步阐、步骘、仓慈、曹昂、曹褒、曹彪、曹操、曹炽、曹沖、曹纯、曹芳、曹幹、曹贡、曹衮、曹洪、曹奂、曹徽、曹鉴、曹矩、曹据、曹均、曹峻、曹礼、曹林、曹霖、曹髦、曹茂、曹丕、曹仁、曹蕤、曹叡、曹爽、曹铄、曹羲、曹协、曹熊、曹休、曹玹、曹训、曹俨、曹邕、曹宇、曹彰、曹肇、曹真、曹植、常播、常林、陈本、陈表、陈登、陈宫、陈矫、陈琳、陈群、陈容、陈泰、陈温、陈武、陈修、陈震、陈祗、程秉、程普、程晓、程昱、崔林、崔琰、单固、邓艾、邓羲、邓飏、邓芝、典韦、丁奉、丁览、丁谥、丁仪、丁廙、董和、董恢、董厥、董袭、董遇、董允、董昭、董卓、杜畿、杜宽、杜夔、杜琼、杜恕、杜微、杜袭、杜挚、法正、樊阿、樊建、繁钦、范慎、费诗、费祎、逢纪、傅嘏、傅巽、甘宁、高柔、顾承、顾邵、顾谭、顾雍、关靖、关羽、管辂、管宁、郭淮、郭嘉、郭配、郭汜、郭宪、国渊、寒贫、韩邦、韩当、韩浩、韩珩、韩暨、韩嵩、浩周、何定、何夔、何苗、何晏、何祗、和洽、贺齐、贺邵、胡沖、胡潜、胡昭、胡质、胡综、扈累、华覈、华佗、华歆、桓范、桓嘉、桓阶、桓陵、桓威、黄崇、黄盖、黄皓、黄朗、黄权、黄忠、霍峻、霍弋、吉茂、纪陟、贾洪、贾逵、贾龙、贾诩、简雍、姜维、蒋斌、蒋济、蒋钦、蒋琬、蒋显、焦先、金尚、金旋、沮授、句扶、厥机、阚泽、孔乂、蒯越、来敏、乐进、乐详、李典、李丰、李孚、李衡、李恢、李傕、李权、李胜、李肃、李通、李严、李义、李譔、凉茂、梁习、梁寓、廖化、廖立、凌统、刘巴、刘备、刘表、刘禅、刘琮、刘惇、刘放、刘封、刘阜、刘馥、刘基、刘靖、刘理、刘敏、刘劭、刘陶、刘先、刘璿、刘焉、刘琰、刘繇、刘晔、刘廙、刘永、刘璋、刘桢、留赞、娄圭、楼玄、卢毓、鲁肃、陆绩、陆凯、陆抗、陆瑁、陆逊、陆祎、陆胤、路粹、吕布、吕岱、吕范、吕据、吕凯、吕蒙、吕虔、吕乂、骆统、麻余、马超、马钧、马良、马茂、马谡、马忠、满宠、满伟、毛玠、孟光、孟仁、弥加、麋竺、缪袭、沐并、难楼、聂友、潘濬、潘勖、潘璋、潘翥、庞德、庞统、庞淯、裴儁、裴潜、裴玄、彭羕、牵招、谯同、谯周、秦宓、全琮、全绪、任峻、阮瑀、邵畴、射援、沈珩、审配、石伟、时苗、史涣、士徽、士匡、士燮、士壹、是仪、苏林、苏愉、苏则、素利、眭固、孙霸、孙贲、孙策、孙綝、孙登、孙奋、孙辅、孙该、孙观、孙皓、孙和、孙桓、孙奂、孙坚、孙皎、孙静、孙俊、孙峻、孙匡、孙礼、孙亮、孙邻、孙虑、孙乾、孙权、孙韶、孙邵、孙松、孙休、孙翊、孙瑜、孙资、蹋顿、唐固、唐咨、陶谦、滕胤、田畴、田丰、田豫、王弼、王粲、王昶、王沖、王蕃、王观、王基、王经、王朗、王连、王烈、王淩、王平、王谦、王叡、王嗣、王肃、王象、王雄、王修、王仪、韦诞、韦康、韦曜、隗禧、卫继、卫觊、卫旌、卫臻、卫兹、位宫、魏滕、魏延、温恢、文聘、文钦、乌延、吴范、吴景、吴普、吴质、吾粲、五梁、伍琼、武周、郤正、向宠、向朗、向条、谢承、谢景、谢渊、辛敞、辛毗、邢颙、徐幹、徐晃、徐琨、徐邈、徐盛、徐庶、徐详、徐宣、徐奕、徐真、许褚、许慈、许混、许靖、许攸、许允、薛悌、薛夏、薛珝、薛莹、薛综、荀甝、荀诜、荀纬、荀衍、荀霬、荀攸、荀俣、荀彧、荀恽、严幹、严畯、严象、阎圃、阎柔、阎温、颜斐、羊衜、杨阜、杨洪、杨俊、杨沛、杨戏、杨修、杨仪、杨综、伊籍、殷礼、尹默、应玚、应璩、于禁、虞昺、虞翻、虞汜、虞耸、虞忠、袁涣、袁侃、袁尚、袁绍、袁术、袁谭、臧霸、臧洪、臧旻、枣祗、笮融、栈潜、张承、张敦、张范、张飞、张奋、张恭、张郃、张纮、张缉、张既、张臶、张就、张辽、张鲁、张猛、张邈、张任、张尚、张悌、张温、张羡、张休、张绣、张玄、张俨、张燕、张杨、张嶷、张裔、张翼、张裕、张昭、张咨、赵达、赵娥、赵俨、赵昱、赵云、赵咨、甄洛、徵崇、郑浑、郑泉、郑泰、郑胄、脂习、钟会、钟繇、钟毓、州泰、周毖、周鲂、周群、周泰、周昕、周宣、周瑜、朱才、朱桓、朱绩、朱据、朱灵、朱然、朱异、朱治、宗预、步度根、曹子乘、曹子棘、曹子京、曹子勤、曹子上、曹子整、高堂隆、公孙度、公孙恭、公孙晃、公孙康、公孙渊、公孙瓒、毌丘甸、毌丘俭、邯郸淳、胡母班、胡伟度、轲比能、令狐邵、令狐愚、刘雄鸣、马日磾、濮阳兴、丘力居、司马朗、司马岐、司马芝、苏仆延、孙宾硕、孙叔然、太史慈、夏侯霸、夏侯称、夏侯惇、夏侯和、夏侯衡、夏侯惠、夏侯楙、夏侯荣、夏侯尚、夏侯威、夏侯玄、夏侯渊、鲜于辅、杨阿若、伊夷模、钟离牧、钟离徇、周生烈、朱建平、诸葛诞、诸葛瑾、诸葛恪、诸葛亮、诸葛乔、诸葛融、诸葛瞻";
        nameList = names.split("、");
    }

    private static final Logger log = LoggerFactory.getLogger(RtcHandler.class);

    private static AtomicInteger onlineCount = new AtomicInteger(0);

    private static ConcurrentHashMap<String, RtcHandler> webSocketMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Set<String>> groupMap = new ConcurrentHashMap<>();

    private String sid;

    private Session session;

    private UserInfo userInfo;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.sid = sid;

        Set<String> sets = groupMap.computeIfAbsent(sid, (s) -> ConcurrentHashMap.newKeySet());
        this.session = session;
        String userId = session.getId();
        sets.add(userId);
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
        } else {
            webSocketMap.put(userId, this);
            addOnlineCount();
        }

        userInfo = new UserInfo();
        userInfo.setId(session.getId());
        userInfo.setName(nameList[RandomUtils.nextInt(0, nameList.length)]);
        log.info("用户连接:" + userId + ",当前在线人数为:" + getOnlineCount());
        try {
            Message message = new Message();
            message.setType(Message.Type.INIT.getType());
            Map<String, Object> data = new HashMap<>();
            data.put("iceServers", IceService.getIceServers());
            data.put("userInfo", userInfo);
            message.setData(data);
            sendMessage(message);
        } catch (IOException e) {
            log.error("用户:" + userId + ",网络异常!!!!!!");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws IOException {
        String userId = userInfo.getId();
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            Set<String> sets = groupMap.get(this.sid);
            sets.remove(userId);
            synchronized (RtcHandler.class) {
                if (sets.isEmpty()) {
                    groupMap.remove(this.sid);
                }
            }
            subOnlineCount();
        }
        log.info("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param messageText 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String messageText) throws IOException {
        if (StringUtils.isBlank(messageText)) {
            return;
        }
        Message message = getObjectMapper().readValue(messageText, Message.class);
        if (Message.Type.PING.getType().equals(message.getType())) {
            Message pong = new Message();
            pong.setType(Message.Type.PONG.getType());
            sendMessage(pong);
            return;
        }
        log.info("用户消息:" + this.session.getId() + ",报文:" + messageText);
        message.setSender(userInfo.getId());
        String sendTo = message.getReceiver();
        if (sendTo != null) {
            webSocketMap.get(sendTo).sendMessage(message);
            return;
        }
        Set<String> sets = groupMap.get(sid);
        if (sets == null || sets.isEmpty()) {
            return;
        }
        sets.forEach(userId -> {
            try {
                RtcHandler v = webSocketMap.get(userId);
                if (v != this) {
                    v.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + session.getId() + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public synchronized void sendMessage(Message message) throws IOException {
        this.session.getBasicRemote().sendText(getObjectMapper().writeValueAsString(message));
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }


    public static int getOnlineCount() {
        return onlineCount.get();
    }

    public static void addOnlineCount() {
        onlineCount.incrementAndGet();
    }

    public static void subOnlineCount() {
        onlineCount.decrementAndGet();
    }

    private ObjectMapper getObjectMapper() {
        return JsonUtil.getObjectMapper();
    }
}
