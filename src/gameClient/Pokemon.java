package gameClient;
import api.edge_data;
import gameClient.util.Point3D;

import java.util.Objects;

public class Pokemon {
	private edge_data _edge;
	private double _value;
	private int _type;
	private Point3D _pos;
	private double min_dist;
	private int from;
	private int to;
	private double worth;

	public Pokemon(Point3D p, int t, double v, double s, edge_data e) {
		_type = t;
		_value = v;
		_edge = e;
		_pos = p;
		min_dist = -1;
		from = -1;
	}
	public edge_data get_edge() {return _edge;}
	public void set_edge(edge_data _edge) {this._edge = _edge;}
	public Point3D getLocation() { return _pos;}
	public void setLocation(Point3D pos) { this._pos = pos;}
	public int getType() {return _type;}
	public void setType(int type) {this._type = type;}
	public double getValue() {return _value;}
	public void setValue(int _value) {this._value = _value; }
	public double getMin_dist() {return min_dist;}
	public void setMin_dist(double mid_dist) {this.min_dist = mid_dist;}
	public int getFrom() {return from;}
	public void setFrom(int from) {this.from = from;}
	public int getTo() {return to;}
	public void setTo(int from) {this.to = from;}
	public double getWorth() {
		return worth;
	}
	public void setWorth(double worth) {
		this.worth = worth;
	}

	//	@Override
	public String toString() {
		return "Pokemon{" +
				"_edge=" + _edge +
				", _value=" + _value +
				", _type=" + _type +
				", _pos=" + _pos +
				", min_dist=" + min_dist +
				", min_ro=" + from +
				'}';
	}
	/*
	@Override
	public String toString() {
		return "Pokemon{" +
				", _value=" + _value +
				'}';
	}*/
	public void deepCopy(Pokemon other) {
		this._edge = other.get_edge();
		this._value = other.getValue();
		this._type = other.getType();
		this._pos = other.getLocation();
		this.min_dist = other.getMin_dist();
		this.from = other.getFrom();
		this.to = other.getTo();
		this.worth = other.getWorth();
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pokemon pokemon = (Pokemon) o;
		return _type == pokemon._type && Objects.equals(_pos, pokemon._pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_type, _pos);
	}
}
