package cl.techk.adjudicadas.carga;

import java.util.LinkedHashMap;

import cl.techk.adjudicadas.carga.Maestro_compG;
import cl.techk.adjudicadas.carga.Maestro_compGAux;
import cl.techk.adjudicadas.carga.Maestro_compM;
import cl.techk.adjudicadas.carga.Maestro_compMAux;
import cl.techk.adjudicadas.carga.Maestro_prod;
import cl.techk.adjudicadas.carga.Maestro_prodAux;
import cl.techk.adjudicadas.carga.Maestro_prov;
import cl.techk.adjudicadas.carga.Maestro_provAux;

public class Maestros_Hash {
	// MAESTROS PRINCIPALES
	public static LinkedHashMap<String, Maestro_prov> MaestrosProv = new LinkedHashMap<String, Maestro_prov>();
	public static LinkedHashMap<String, Maestro_compG> MaestrosCompG = new LinkedHashMap<String, Maestro_compG>();
	public static LinkedHashMap<Integer, Maestro_compM> MaestrosCompM = new LinkedHashMap<Integer, Maestro_compM>();
	public static LinkedHashMap<Integer, Maestro_prod> MaestrosProd = new LinkedHashMap<Integer, Maestro_prod>();

	// LISTA DE MAESTROS NUEVOS O MODIFICADOS.
	public static LinkedHashMap<String, Maestro_provAux> MaestrosProvAux = new LinkedHashMap<String, Maestro_provAux>();
	public static LinkedHashMap<String, Maestro_compGAux> MaestrosCompGAux = new LinkedHashMap<String, Maestro_compGAux>();
	public static LinkedHashMap<String, Maestro_compMAux> MaestrosCompMAux = new LinkedHashMap<String, Maestro_compMAux>();
	public static LinkedHashMap<Integer, Maestro_prodAux> MaestrosProdAux = new LinkedHashMap<Integer, Maestro_prodAux>();

}
