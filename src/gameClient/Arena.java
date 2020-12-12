package gameClient;

import api.*;
import gameClient.util.Point3D;
import gameClient.util.Range;
import gameClient.util.Range2D;
import gameClient.util.Range2Range;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class represents a multi Agents Arena which move on a graph - grabs Pokemons and avoid the Zombies.
 * @author boaz.benmoshe
 *
 */
public class Arena {

	private game_service _game;
	public static final double EPS1 = 0.001, EPS2=EPS1*EPS1;
	private directed_weighted_graph _graph;
	private List<Agent> _agents = new ArrayList<>();
	private List<Pokemon> _pokemons;
	private List<String> _info;
	private dw_graph_algorithms _algo;
//	HashMap<Integer, Queue<node_data>> map = new HashMap<>();
	HashMap<Integer, Pokemon> map = new HashMap<>();

	//========================= CONSTRUCTORS ===========================

	public Arena(game_service game) {
		_info = new ArrayList<>();
		_game = game;
		_graph = graphJsonToGraph(game.getGraph());
		System.out.println(_graph);

		_algo = new DWGraph_Algo();
		_algo.init(_graph);
		System.out.println("is connected: "+_algo.isConnected());

		_pokemons = json2Pokemons(game.getPokemons());
		initAgents();

		exportJsonToFile("GameJSON", game.toString());
		exportJsonToFile("GameGraph", game.getGraph());
		exportJsonToFile("GameAgents", game.getAgents());
		exportJsonToFile("GamePokemons", game.getPokemons());
	}

	//========================== JSON CONVERTING ==============================

	public void initAgents(){
		String info = _game.toString();
		JSONObject line;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			int rs = ttt.getInt("agents");
			System.out.println(info);
			System.out.println(_game.getPokemons());
			int src_node = 0;  // arbitrary node, you should start at one of the pokemon
			ArrayList<Pokemon> cl_fs = json2Pokemons(_game.getPokemons());
			for(int a = 0; a<cl_fs.size(); a++) {
				Arena.updateEdge(cl_fs.get(a), _graph);
			}
			for(int a = 0; a < rs ;a++) {
				int ind = a%cl_fs.size();
				Pokemon c = cl_fs.get(ind);
				int nn = c.get_edge().getDest();
				if(c.getType()<0 ) {
					nn = c.get_edge().getSrc();
				}

				_game.addAgent(nn);
			}
		}
		catch (JSONException e) {e.printStackTrace();}
	}

	public List<Agent> getAgents(String aa) {
		ArrayList<Agent> ans = new ArrayList<>();
		try {
			JSONObject ttt = new JSONObject(aa);
			JSONArray ags = ttt.getJSONArray("Agents");
			for(int i=0;i<ags.length();i++) {
				Agent c = new Agent(_graph ,0);
				c.update(ags.get(i).toString());
				ans.add(c);
			}
			//= getJSONArray("Agents");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ans;
	}

	private ArrayList<Pokemon> json2Pokemons(String json) {
		ArrayList<Pokemon> ans = new ArrayList<>();
		try {
			JSONObject ttt = new JSONObject(json);
			JSONArray ags = ttt.getJSONArray("Pokemons");
			for(int i=0; i<ags.length(); i++) {
				JSONObject pp = ags.getJSONObject(i);
				JSONObject pk = pp.getJSONObject("Pokemon");
				int t = pk.getInt("type");
				double v = pk.getDouble("value");
				String p = pk.getString("pos");
				Pokemon f = new Pokemon(new Point3D(p), t, v, 0, null);
				updateEdge(f, _graph);
				ans.add(f);
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		ans.sort((o1, o2) -> {
			if(o1.getValue() > o2.getValue())
				return 1;
			else if(o1.getValue() < o2.getValue())
				return -1;
			else return 0;
		});
		return ans;
	}

	private directed_weighted_graph graphJsonToGraph(String json){
		dw_graph_algorithms ga = new DWGraph_Algo();
		exportJsonToFile("graph",_game.getGraph());
		ga.load("jsonsFiles/graph.json");
		return ga.getGraph();
	}
	private void exportJsonToFile(String path, String json) {
		try {
			String j = (new JSONObject(json)).toString(4);
			File file = new File("jsonsFiles/" + path + ".json");
			FileWriter myWriter = new FileWriter(file);
			myWriter.write(j);
			myWriter.close();
		}catch ( IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	//================================ EDGES ====================================

	public static void updateEdge(Pokemon fr, directed_weighted_graph g) {
		Iterator<node_data> itr = g.getV().iterator();
		while(itr.hasNext()) {
			node_data v = itr.next();
			Iterator<edge_data> iter = g.getE(v.getKey()).iterator();
			while(iter.hasNext()) {
				edge_data e = iter.next();
				boolean f = isOnEdge(fr.getLocation(), e,fr.getType(), g);
				if(f) {fr.set_edge(e);}
			}
		}
	}

	private static boolean isOnEdge(geo_location p, geo_location src, geo_location dest ) {
		boolean ans = false;
		double dist = src.distance(dest);
		double d1 = src.distance(p) + p.distance(dest);
		if(dist>d1-EPS2) {ans = true;}
		return ans;
	}
	private static boolean isOnEdge(geo_location p, int s, int d, directed_weighted_graph g) {
		geo_location src = g.getNode(s).getLocation();
		geo_location dest = g.getNode(d).getLocation();
		return isOnEdge(p,src,dest);
	}
	private static boolean isOnEdge(geo_location p, edge_data e, int type, directed_weighted_graph g) {
		int src = g.getNode(e.getSrc()).getKey();
		int dest = g.getNode(e.getDest()).getKey();
		if(type<0 && dest>src) {return false;}
		if(type>0 && src>dest) {return false;}
		return isOnEdge(p,src, dest, g);
	}

	//============================= GRAPH RANGE =================================

	private static Range2D GraphRange(directed_weighted_graph g) {
		Iterator<node_data> itr = g.getV().iterator();
		double x0=0,x1=0,y0=0,y1=0;
		boolean first = true;
		while(itr.hasNext()) {
			geo_location p = itr.next().getLocation();
			if(first) {
				x0=p.x(); x1=x0;
				y0=p.y(); y1=y0;
				first = false;
			}
			else {
				if(p.x()<x0) {x0=p.x();}
				if(p.x()>x1) {x1=p.x();}
				if(p.y()<y0) {y0=p.y();}
				if(p.y()>y1) {y1=p.y();}
			}
		}
		Range xr = new Range(x0,x1);
		Range yr = new Range(y0,y1);
		return new Range2D(xr,yr);
	}
	public static Range2Range w2f(directed_weighted_graph g, Range2D frame) {
		Range2D world = GraphRange(g);
		Range2Range ans = new Range2Range(world, frame);
		return ans;
	}

	//=========================== GETTERS & SETTERS ================================

	public void setPokemons(List<Pokemon> f) {this._pokemons = f;}
	public void setAgents(List<Agent> f) {this._agents = f;}
	public void setGraph(directed_weighted_graph g) {this._graph =g;}
	public void set_info(List<String> _info) {this._info = _info;}

	public List<Agent> JsonToAgents() {
		return _agents;
//		return getAgents(_game.getAgents());
	}
	public List<Pokemon> getPokemons() {return _pokemons;}
	public directed_weighted_graph getGraph() {return _graph;}
	public List<String> get_info() { return _info;}


	public void moveAgents() {
		_agents = getAgents(_game.move());

//		if(_agents.isEmpty()){
//			_agents = getAgents(_game.move());
//		}
//		for(Agent ag : _agents) {
//			if (ag.Q().isEmpty())
//				_agents = getAgents(_game.move());
//		}

		this.setPokemons(json2Pokemons(_game.getPokemons()));

		for(Agent ag : _agents) {
			int id = ag.getID();
			int src = ag.getSrcNode();
			double v = ag.getValue();
//			if(ag.Q().isEmpty()){
//				nextNode(ag, src);
//			}
//			else{
//				int dst = ag.Q().remove().getKey();
//				_game.chooseNextEdge(ag.getID(), dst);
//				System.out.println("Agent: "+id+", val: "+v+" to node: "+dst);
//				ag.setNextNode(dst);
//			}
			if(!ag.isMoving()) {
				int dest = nextNode(ag, src);
				_game.chooseNextEdge(ag.getID(), dest);
//				System.out.println("Agent: "+id+", val: "+v+" turned to node: "+dest);
				ag.setNextNode(dest);
			}
		}
	}
	private int nextNode(Agent ag, int src) {
		System.out.println("============= start ==============");
		//update pokemons from server
		_pokemons = json2Pokemons(_game.getPokemons());
		for(Pokemon p : _pokemons){
			int type = p.getType();
			edge_data e = p.get_edge();
			if(type == -1){
				p.setFrom(Math.max(e.getSrc(), e.getDest()));
				p.setTo(Math.min(e.getSrc(), e.getDest()));
			}
			else{
				p.setFrom(Math.min(e.getSrc(), e.getDest()));
				p.setTo(Math.max(e.getSrc(), e.getDest()));
			}
			double extra = p.getLocation().distance(_graph.getNode(p.getFrom()).getLocation());
			p.setMin_dist(_algo.shortestPathDist(src, p.getFrom()) + extra);
			p.setWorth(p.getValue()-p.getMin_dist());
			System.out.println("Candidate distance: " + p.getMin_dist() + ", "+p.getLocation());
		}

		Pokemon chosen = _pokemons.get(0);

		double max = Double.MIN_VALUE;
		for(Pokemon p : _pokemons){
			if(available(p, ag)){
				double w = p.getWorth();
				if (w>max) {
					max = w;
					chosen = p;
				}
			}
		}
		System.out.println("minimum distance: "+ chosen.getMin_dist()+ ", " + chosen.getLocation());
		map.put(ag.getID(), chosen);
		ag.path = _algo.shortestPath(src, chosen.getFrom());
		ag.path.add(_graph.getNode(chosen.getTo()));
		System.out.println("agent:"+ag.getID()+" chose:"+chosen.get_edge()+" path: "+ag.path);
		System.out.println("============= end ==============");
		return ag.path.get(1).getKey();
	}

	private boolean available(Pokemon p, Agent a) {
		for(Agent ag : _agents){
			if(a.getID()!=ag.getID() && p.equals(map.get(ag.getID())))
				return false;
		}
		return true;
	}


}
