package client.net;
import client.model.RoomInfo;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


	public class NetStub {
		private static final NetStub INSTANCE = new NetStub();
		public static NetStub i() { return INSTANCE; }
		private NetStub() {}
		private final Map<String, String> users = new ConcurrentHashMap<>(); // user -> pass
		private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>(); // roomId -> members
		private String currentUser = null;
		public interface Listener {
			void onOk(String message);
			void onError(String message);
			void onRoomList(List<RoomInfo> rooms);
			void onRoomJoined(String roomId);
			void onChat(String roomId, String from, String text, long ts);
			}
		private final List<Listener> listeners = new ArrayList<>();
		public void addListener(Listener l){ listeners.add(l); }
		public void removeListener(Listener l){ listeners.remove(l); }
		private void emitUI(Runnable r){ SwingUtilities.invokeLater(r); }
		public void register(String username, String password){
			if(username == null || username.isBlank() || password == null || password.isBlank()){
			emitUI(() -> listeners.forEach(l -> l.onError("Username/Password rỗng")));
			return;
			}
			if(users.containsKey(username)){
			emitUI(() -> listeners.forEach(l -> l.onError("Username đã tồn tại")));
			return;
			}
			users.put(username, password);
			emitUI(() -> listeners.forEach(l -> l.onOk("Đăng ký thành công")));
			}
			public void login(String username, String password){
			if(!users.containsKey(username) || !Objects.equals(users.get(username), password)){
			emitUI(() -> listeners.forEach(l -> l.onError("Sai username hoặc password")));
			return;
			}
			currentUser = username;
			emitUI(() -> listeners.forEach(l -> l.onOk("Đăng nhập thành công")));
			}
			public String getCurrentUser(){ return currentUser; }
			// === Rooms ===
			public void createRoom(){
			ensureLogin();
			String id = UUID.randomUUID().toString().substring(0,6);
			rooms.putIfAbsent(id, Collections.synchronizedSet(new HashSet<>()));
			rooms.get(id).add(currentUser);
			String rid = id;
			emitUI(() -> listeners.forEach(l -> l.onRoomJoined(rid)));
			// broadcast system chat
			broadcast(rid, "SYSTEM", currentUser + " created room");
			}
			public void listRooms(){
			List<RoomInfo> list = new ArrayList<>();
			for(var e : rooms.entrySet()){
			list.add(new RoomInfo(e.getKey(), e.getValue().size()));
			}
			list.sort(Comparator.comparing(RoomInfo::roomId));
			emitUI(() -> listeners.forEach(l -> l.onRoomList(list)));
			}
			public void joinRandom(){
				ensureLogin();
				String rid = rooms.keySet().stream().findAny().orElseGet(() -> {
				String id = UUID.randomUUID().toString().substring(0,6);
				rooms.put(id, Collections.synchronizedSet(new HashSet<>()));
				return id;
				});
				rooms.get(rid).add(currentUser);
				String finalRid = rid;
				emitUI(() -> listeners.forEach(l -> l.onRoomJoined(finalRid)));
				broadcast(finalRid, "SYSTEM", currentUser + " joined");
				}
			public void joinById(String rid){
				ensureLogin();
				if(!rooms.containsKey(rid)){
				emitUI(() -> listeners.forEach(l -> l.onError("Phòng không tồn tại")));
				return;
				}
				rooms.get(rid).add(currentUser);
				emitUI(() -> listeners.forEach(l -> l.onRoomJoined(rid)));
				broadcast(rid, "SYSTEM", currentUser + " joined");
				}
			public void sendChat(String rid, String text){
				ensureLogin();
				if(!rooms.containsKey(rid)){
				emitUI(() -> listeners.forEach(l -> l.onError("Phòng không tồn tại")));
				return;
				}
				broadcast(rid, currentUser, text);
				}
			private void broadcast(String rid, String from, String text){
				long ts = System.currentTimeMillis();
				emitUI(() -> listeners.forEach(l -> l.onChat(rid, from, text, ts)));
				}


				private void ensureLogin(){ if(currentUser == null) throw new IllegalStateException("Chưa đăng nhập"); }
				}