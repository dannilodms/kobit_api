package br.com.kobit.web_api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import br.com.kobit.web_api.config.FluigConnectionFactory;
import br.com.kobit.web_api.config.LogixConnectionFactory;
import br.com.kobit.web_api.controller.DTO.VeiculoDTO;
import br.com.kobit.web_api.error.ErrorStatus;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
@Path("/api")
public class DefaultApiController {

    @GET
    @Path("/GetTeste")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response GetTeste() {
        return Response.ok("OK").build();
    }

    @POST
    @Path("/getTiposDeVeiculos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTiposVeiculos(VeiculoDTO veiculo) {
        var query = "SELECT ID, VEICULO FROM MON_VEICULOS WHERE 1=1 ";
        if (veiculo.getId() > 0)
            query += (" and id = " + veiculo.getId());
        if (veiculo.getVeiculo() != null && !veiculo.getVeiculo().isEmpty())
            query += (" and veiculo = '" + veiculo.getVeiculo() + "'");
        try (final var connection = FluigConnectionFactory.getConnection();
                final var statement = connection.prepareStatement(query)) {
            final var resultSet = statement.executeQuery();
            final var veiculos = new ArrayList<VeiculoDTO>();
            while (resultSet.next()) {
                final var id = resultSet.getInt("ID");
                final var veiculoDesc = resultSet.getString("VEICULO");
                veiculos.add(new VeiculoDTO(id, veiculoDesc));
            }
            return Response.ok(veiculos).build();
        } catch (Exception e) {
            log.error("[kobit_api] Erro ao buscar tipos de veiculos", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorStatus(e)).build();
        }
    }

    @GET
    @Path("/GetSegmentos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSegmentos() {
        var query = "SELECT ID, DESCRICAO FROM MON_GESTAOPROPOSTA_SEGMENTO ORDER BY ID";
        ;
        try (final var connection = FluigConnectionFactory.getConnection();
                final var statement = connection.prepareStatement(query)) {
            final var resultSet = statement.executeQuery();
            final var segmentos = new ArrayList<Map<String, String>>();
            while (resultSet.next()) {
                final var segmento = new HashMap<String, String>();
                segmento.put("ID", resultSet.getString("ID"));
                segmento.put("DESCRICAO", resultSet.getString("DESCRICAO"));
                segmentos.add(segmento);
            }
            return Response.ok(segmentos).build();
        } catch (Exception e) {
            log.error("[kobit_api] Erro ao buscar segmentos", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorStatus(e)).build();
        }
    }

    @GET
    @Path("/ZoomClientes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response zoomClientes(@QueryParam("search") String search, @QueryParam("qntdPorPagina") String qntdPorPagina,
            @QueryParam("pagina") String pagina, @QueryParam("dashboard") boolean dashboard) {
        var query = "";

        if (dashboard) {
            query = "SELECT COD_CLIENTE, NUM_CGC_CPF, NOM_CLIENTE FROM (SELECT a.COD_CLIENTE, a.NUM_CGC_CPF, a.NOM_CLIENTE, rownum row_num FROM (SELECT COD_CLIENTE, NUM_CGC_CPF, NOM_CLIENTE FROM LOGIX10PRD.CLIENTES "
                    +
                    " WHERE COD_CLIENTE IN (SELECT DISTINCT COD_CLIENTE FROM LOGIX10PRD.MON_GESTAOPROPOSTA_DASHBOARD) "
                    + ((search != null && !search.isEmpty())
                            ? ("AND (UPPER(COD_CLIENTE) LIKE '%" + search.toUpperCase() + "%' OR " +
                                    "UPPER(NOM_CLIENTE) LIKE '%" + search.toUpperCase() + "%' OR " +
                                    "NUM_CGC_CPF LIKE '%" + search + "%') ")
                            : "")
                    +
                    "ORDER BY NOM_CLIENTE " +
                    ") a " +
                    "WHERE rownum < ((" + pagina + " * " + qntdPorPagina + ") + 1 )" +
                    ") " +
                    "WHERE row_num >= (((" + pagina + "-1) * " + qntdPorPagina + ") + 1)";
        } else {
            query = "SELECT COD_CLIENTE, NUM_CGC_CPF, NOM_CLIENTE FROM (SELECT a.COD_CLIENTE, a.NUM_CGC_CPF, a.NOM_CLIENTE, rownum row_num FROM (SELECT COD_CLIENTE, NUM_CGC_CPF, NOM_CLIENTE FROM LOGIX10PRD.CLIENTES "
                    +
                    "WHERE 1 = 1 "
                    + ((search != null && !search.isEmpty())
                            ? ("AND (UPPER(COD_CLIENTE) LIKE '%" + search.toUpperCase() + "%' OR " +
                                    "UPPER(NOM_CLIENTE) LIKE '%" + search.toUpperCase() + "%' OR " +
                                    "NUM_CGC_CPF LIKE '%" + search + "%') ")
                            : "")
                    +
                    "ORDER BY NOM_CLIENTE " +
                    ") a " +
                    "WHERE rownum < ((" + pagina + " * " + qntdPorPagina + ") + 1 )" +
                    ") " +
                    "WHERE row_num >= (((" + pagina + "-1) * " + qntdPorPagina + ") + 1)";
        }

        try (final var connection = LogixConnectionFactory.getConnection();
                final var statement = connection.prepareStatement(query)) {
            final var resultSet = statement.executeQuery();
            final var clientes = new ArrayList<Map<String, String>>();
            while (resultSet.next()) {
                final var cliente = new HashMap<String, String>();
                cliente.put("COD_CLIENTE", resultSet.getString("COD_CLIENTE"));
                cliente.put("NUM_CGC_CPF", resultSet.getString("NUM_CGC_CPF"));
                cliente.put("NOM_CLIENTE", resultSet.getString("NOM_CLIENTE"));
                clientes.add(cliente);
            }
            return Response.ok(clientes).build();
        } catch (Exception e) {
            log.error("[kobit_api] Erro ao buscar clientes", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorStatus(e)).build();
        }
    }

    @GET
    @Path("/GetCliente")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCliente(@QueryParam("cnpj") String cnpj, @QueryParam("dashboard") boolean dashboard) {
        var query = "";
        final var cnpjFinal = cnpj.length() == 18 ? cnpj : "0" + cnpj;

        if (dashboard) {
            query = "SELECT COD_CLIENTE, NOM_CLIENTE FROM LOGIX10PRD.CLIENTES " +
                    "WHERE COD_CLIENTE IN (SELECT DISTINCT COD_CLIENTE FROM MON_GESTAOPROPOSTA_DASHBOARD) AND NUM_CGC_CPF = '"
                    + cnpjFinal + "'";
        } else {
            query = "SELECT COD_CLIENTE, NOM_CLIENTE FROM LOGIX10PRD.CLIENTES WHERE NUM_CGC_CPF = '" + cnpjFinal + "'";
        }

        try (final var connection = LogixConnectionFactory.getConnection();
                final var statement = connection.prepareStatement(query)) {
            final var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final var cliente = new HashMap<String, String>();
                cliente.put("codigo", resultSet.getString("COD_CLIENTE"));
                cliente.put("NOM_CLIENTE", resultSet.getString("nome"));
                return Response.ok(cliente).build();
            }
            return Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("[kobit_api] Erro ao buscar clientes", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorStatus(e)).build();
        }
    }

    @GET
    @Path("/GetUnidadeBrado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnidadeBrado(@QueryParam("codigo") String codigo, @QueryParam("dashboard") boolean dashboard) {
        var query = "";

        if (dashboard) {
            query = "SELECT DEN_EMPRESA FROM LOGIX10PRD.EMPRESA WHERE COD_EMPRESA IN (SELECT DISTINCT UNIDADE_NEGOCIO FROM MON_GESTAOPROPOSTA_DASHBOARD) AND COD_EMPRESA = '"
                    + codigo + "'";
        } else {
            query = "SELECT DEN_EMPRESA FROM LOGIX10PRD.EMPRESA WHERE COD_CLIENTE IN ('003307926000970', '003307926001003', '003307926001607', '003307926003057') AND COD_EMPRESA = '"
                    + codigo + "'";
        }

        try (final var connection = LogixConnectionFactory.getConnection();
                final var statement = connection.prepareStatement(query)) {
            final var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final var unidade = new HashMap<String, String>();
                unidade.put("DEN_EMPRESA", resultSet.getString("DEN_EMPRESA"));
                return Response.ok(unidade).build();
            }
            return Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("[kobit_api] Erro ao buscar unidade brado", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorStatus(e)).build();
        }
    }

    @GET
    @Path("/ZoomUnidadesBrado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response zoomUnidadesBrado(@QueryParam("search") String search,
            @QueryParam("qntdPorPagina") String qntdPorPagina, @QueryParam("pagina") String pagina,
            @QueryParam("dashboard") boolean dashboard) {
        var query = "";
        if (dashboard) {
            query = "SELECT COD_EMPRESA, DEN_EMPRESA FROM (SELECT a.COD_EMPRESA, a.DEN_EMPRESA, rownum row_num FROM (SELECT COD_EMPRESA, DEN_EMPRESA FROM LOGIX10PRD.EMPRESA "
                    +
                    "WHERE COD_EMPRESA IN (SELECT DISTINCT UNIDADE_NEGOCIO FROM MON_GESTAOPROPOSTA_DASHBOARD) "
                    + ((search != null && !search.isEmpty())
                            ? ("AND (UPPER(COD_EMPRESA) LIKE '%" + search.toUpperCase() + "%' OR " +
                                    "UPPER(DEN_EMPRESA) LIKE '%" + search.toUpperCase() + "%') ")
                            : "")
                    +
                    "ORDER BY DEN_EMPRESA " +
                    ") a " +
                    "WHERE rownum < ((" + pagina + " * " + qntdPorPagina + ") + 1 ) " +
                    ") " +
                    "WHERE row_num >= (((" + pagina + "-1) * " + qntdPorPagina + ") + 1)";
        } else {
            query = "SELECT COD_EMPRESA, DEN_EMPRESA FROM (SELECT a.COD_EMPRESA, a.DEN_EMPRESA, rownum row_num FROM (SELECT COD_EMPRESA, DEN_EMPRESA FROM LOGIX10PRD.EMPRESA "
                    +
                    "WHERE COD_CLIENTE IN ('003307926000970', '003307926001003', '003307926001607', '003307926003057') "
                    + ((search != null && !search.isEmpty())
                            ? ("AND (UPPER(COD_EMPRESA) LIKE '%" + search.toUpperCase() + "%' OR " +
                                    "UPPER(DEN_EMPRESA) LIKE '%" + search.toUpperCase() + "%') ")
                            : "")
                    +
                    "ORDER BY DEN_EMPRESA " +
                    ") a " +
                    "WHERE rownum < ((" + pagina + " * " + qntdPorPagina + ") + 1 ) " +
                    ") " +
                    "WHERE row_num >= (((" + pagina + "-1) * " + qntdPorPagina + ") + 1)";
        }

        try (final var connection = LogixConnectionFactory.getConnection();
                final var statement = connection.prepareStatement(query)) {
            final var resultSet = statement.executeQuery();
            final var unidades = new ArrayList<Map<String, String>>();
            while (resultSet.next()) {
                final var unidade = new HashMap<String, String>();
                unidade.put("COD_EMPRESA", resultSet.getString("COD_EMPRESA"));
                unidade.put("DEN_EMPRESA", resultSet.getString("DEN_EMPRESA"));
                unidades.add(unidade);
            }
            return Response.ok(unidades).build();
        } catch (Exception e) {
            log.error("[kobit_api] Erro ao buscar unidade brado", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorStatus(e)).build();
        }
    }
}
