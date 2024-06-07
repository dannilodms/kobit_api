package br.com.kobit.web_api.controller.DTO;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VeiculoDTO implements Serializable{
    private Integer id;
    private String veiculo;
}
