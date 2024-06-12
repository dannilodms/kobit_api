package br.com.kobit.web_api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import br.com.kobit.web_api.config.FluigConnectionFactory;
import br.com.kobit.web_api.error.ErrorStatus;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
@Path("/GestaoPropostaComercial")
public class GestaoPropostaComercialController {

    @GET
    @Path("/GetDadosDashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDadosDashbord(
            @QueryParam("dataInicial") String dataInicial,
            @QueryParam("dataFinal") String dataFinal, @QueryParam("cliente") String cliente,
            @QueryParam("gerente") String gerente, @QueryParam("tipoNegocio") String tipoNegocio,
            @QueryParam("unidade") String unidade, @QueryParam("segmento") String segmento,
            @QueryParam("status") String status) {
        var query = "SELECT FLUIG, R.NOME AS GERENTE, D.CNPJ, D.RAZAO_SOCIAL, TP.DESCRICAO AS TIPO_NEGOCIO, SG.DESCRICAO AS SEGMENTO, E.DEN_EMPRESA AS UNIDADE_NEGOCIO, DATA_INICIO, DATA_FIM, DATA_VIG_INICIO, DATA_VIG_FIM, ST.DESCRICAO AS STATUS FROM MON_GESTAOPROPOSTA_DASHBOARD D "
                +
                "LEFT JOIN LOGIX10PRD.EMPRESA@LOGIX_CONSULTA E ON E.COD_EMPRESA = D.UNIDADE_NEGOCIO " +
                "LEFT JOIN MON_GESTAOPROPOSTA_RESPCOM R ON R.CODIGO = D.GERENTE " +
                "LEFT JOIN MON_GESTAOPROPOSTA_SEGMENTO SG ON SG.ID = D.SEGMENTO " +
                "LEFT JOIN MON_GESTAOPROPOSTA_TIPONEG TP ON TP.ID = D.TIPO_NEGOCIO " +
                "LEFT JOIN MON_GESTAOPROPOSTA_STATUS ST ON ST.ID = D.STATUS " +
                "WHERE 1 = 1 "
                + (dataInicial.equals("") ? "" : ("AND TO_DATE(DATA_INICIO, ''dd/MM/yyyy) >= TO_DATE('" + dataInicial + "', 'dd/MM/yyyy') "))
                + (dataFinal.equals("") ? "" : ("AND TO_DATE(DATA_INICIO, 'dd/MM/yyyy') <= TO_DATE('" + dataFinal + "', 'dd/MM/yyyy') "))
                + (cliente.equals("") ? "" : ("AND D.COD_CLIENTE = '" + cliente + "' "))
                + (gerente.equals("") ? "" : ("AND GERENTE = " + gerente + " "))
                + (tipoNegocio.equals("") ? "" : ("AND TIPO_NEGOCIO = " + tipoNegocio + " "))
                + (unidade.equals("") ? "" : ("AND UNIDADE_NEGOCIO = '" + unidade + "' "))
                + (segmento.equals("") ? "" : ("AND SEGMENTO = " + segmento + " "))
                + (status.equals("") ? "" : ("AND STATUS = " + status + " "));
        try (final var connection = FluigConnectionFactory.getConnection();
                final var statement = connection.prepareStatement(query)) {
            final var resultSet = statement.executeQuery();
            final var dados = new ArrayList<Map<String, String>>();
            while (resultSet.next()) {
                final var dado = new HashMap<String, String>();
                dado.put("FLUIG", resultSet.getString("FLUIG"));
                dado.put("GERENTE", resultSet.getString("GERENTE"));
                dado.put("CNPJ", resultSet.getString("CNPJ"));
                dado.put("RAZAO_SOCIAL", resultSet.getString("RAZAO_SOCIAL"));
                dado.put("TIPO_NEGOCIO", resultSet.getString("TIPO_NEGOCIO"));
                dado.put("SEGMENTO", resultSet.getString("SEGMENTO"));
                dado.put("UNIDADE_NEGOCIO", resultSet.getString("UNIDADE_NEGOCIO"));
                dado.put("DATA_INICIO", resultSet.getString("DATA_INICIO"));
                dado.put("DATA_FIM", resultSet.getString("DATA_FIM"));
                dado.put("DATA_VIG_INICIO", resultSet.getString("DATA_VIG_INICIO"));
                dado.put("DATA_VIG_FIM", resultSet.getString("DATA_VIG_FIM"));
                dado.put("STATUS", resultSet.getString("STATUS"));
                dados.add(dado);
            }
            return Response.ok(dados).build();
        } catch (Exception e) {
            log.error("[kobit_api] Erro ao buscar segmentos", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorStatus(e)).build();
        }
    }

}
