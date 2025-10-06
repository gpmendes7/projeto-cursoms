package io.github.cursodsouza.mscartoes.infra.mqueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cursodsouza.mscartoes.domain.Cartao;
import io.github.cursodsouza.mscartoes.domain.ClienteCartao;
import io.github.cursodsouza.mscartoes.domain.DadosSolicitacaoEmissaoCartao;
import io.github.cursodsouza.mscartoes.infra.repository.CartaoRepository;
import io.github.cursodsouza.mscartoes.infra.repository.ClienteCartaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmissaoCartaoSubscriber {

    private final CartaoRepository cartaoRepository;
    private final ClienteCartaoRepository clienteCartaoRepository;

    @RabbitListener(queues = "${mq.queues.emissao-cartoes}")
    public void receberSoliticitacaoEmissao(@Payload String payload) {
        try {
            var mapper = new ObjectMapper();

            DadosSolicitacaoEmissaoCartao dados = mapper.readValue(payload, DadosSolicitacaoEmissaoCartao.class);
            Cartao cartao = cartaoRepository.findById(dados.getIdCartao()).orElseThrow();

            ClienteCartao clienteCartao = new ClienteCartao();
            clienteCartao.setCartao(cartao);
            clienteCartao.setCpf(dados.getCpf());
            clienteCartao.setLimite(dados.getLimiteLiberado());

            clienteCartaoRepository.save(clienteCartao);
        } catch (Exception e) {
            log.error("Erro ao receber solicitacao de emissao de cartao: {}", e.getMessage());
        }
    }

}
