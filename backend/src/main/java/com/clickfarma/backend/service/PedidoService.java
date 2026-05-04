package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.MedicamentoExtraidoDTO;
import com.clickfarma.backend.dto.PedidoRequestDTO;
import com.clickfarma.backend.dto.PedidoResponseDTO;
import com.clickfarma.backend.dto.ItemPedidoRequestDTO;
import com.clickfarma.backend.dto.RastreioResponseDTO;
import com.clickfarma.backend.model.AgendamentoRecompra;
import com.clickfarma.backend.model.ItemPedido;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Rastreio;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.AgendamentoRecompraRepository;
import com.clickfarma.backend.repository.ItemPedidoRepository;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.RastreioRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import com.clickfarma.backend.repository.MotoboyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private RastreioRepository rastreioRepository;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private AgendamentoRecompraRepository agendamentoRecompraRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private MotoboyRepository motoboyRepository;

    /**
     * Cria o pedido normal (sem considerar agendamento de recompra).
     * Se quiser já criar os agendamentos, use o método
     * criarPedidoComAgendamentos abaixo.
     */
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO pedidoDTO) {
        Usuario usuario = usuarioRepository.findById(pedidoDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Pedido pedido = new Pedido(usuario);
        pedido.setMetodoPagamento(pedidoDTO.getMetodoPagamento());
        pedido.setEnderecoEntrega(pedidoDTO.getEnderecoEntrega() != null ?
                pedidoDTO.getEnderecoEntrega() : usuario.getEndereco());
        pedido.setObservacoes(pedidoDTO.getObservacoes());
        pedido.setStatus(Pedido.StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setDataAtualizacao(LocalDateTime.now());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedidoRequestDTO itemDTO : pedidoDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProdutoId()));

            if (produto.getEstoque() < itemDTO.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            ItemPedido item = new ItemPedido(produto, itemDTO.getQuantidade());
            item.setPedido(pedidoSalvo);

            produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
            produtoRepository.save(produto);

            itemPedidoRepository.save(item);
            valorTotal = valorTotal.add(item.getSubtotal());
        }

        // Mock distance calculation and freight value (R$ 3/km)
        double distanciaKm = Math.round((Math.random() * 5.0 + 1.0) * 100.0) / 100.0;
        BigDecimal valorFrete = BigDecimal.valueOf(distanciaKm * 3.0);
        pedidoSalvo.setDistanciaKm(distanciaKm);
        pedidoSalvo.setValorFrete(valorFrete);

        // Generate validation codes
        String codigoRetirada = String.format("%04d", (int)(Math.random() * 10000));
        String codigoEntrega = String.format("%04d", (int)(Math.random() * 10000));
        pedidoSalvo.setCodigoRetirada(codigoRetirada);
        pedidoSalvo.setCodigoEntrega(codigoEntrega);

        // Add freight to total
        pedidoSalvo.setValorTotal(valorTotal.add(valorFrete));
        pedidoSalvo = pedidoRepository.save(pedidoSalvo);

        String metodo = pedidoDTO.getMetodoPagamento() != null
                ? pedidoDTO.getMetodoPagamento().trim().toUpperCase(Locale.ROOT)
                : "";

        String linkPagamento = null;
        if ("MERCADO_PAGO".equals(metodo)) {
            // Fluxo real: cria link e redireciona.
            linkPagamento = pagamentoService.criarLinkPagamento(valorTotal.doubleValue(), pedidoSalvo.getId());
        } else {
            // Fluxo demo: finaliza o pedido sem redirecionar e já cria rastreio para apresentação/teste.
            finalizarPedidoSimulado(pedidoSalvo);
        }

        PedidoResponseDTO responseDTO = new PedidoResponseDTO(pedidoSalvo);
        responseDTO.setLinkPagamento(linkPagamento);

        emailNotificationService.enviarConfirmacaoPedido(usuario, pedidoSalvo, linkPagamento);

        return responseDTO;
    }

    private void finalizarPedidoSimulado(Pedido pedido) {
        if (pedido == null) return;

        pedido.setStatus(Pedido.StatusPedido.PAGO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedidoRepository.save(pedido);

        // Cria um rastreio simples para permitir consulta imediata via numero/codigo do pedido.
        if (pedido.getRastreio() == null) {
            Rastreio rastreio = new Rastreio(pedido);
            rastreio.setTransportadora("ClickFarma Express (Demo)");
            rastreio.setDataEnvio(LocalDateTime.now());
            rastreio.setDataPrevisaoEntrega(LocalDateTime.now().plusDays(5));
            rastreio.setStatus("EM_TRANSITO");
            rastreio.setUltimaLocalizacao("Centro de Distribuição");
            rastreio.setUltimaAtualizacao(LocalDateTime.now());
            rastreio = rastreioRepository.save(rastreio);
            // Garante o vínculo bidirecional para consultas via p.rastreio (JOIN FETCH).
            pedido.setRastreio(rastreio);

            pedido.setStatus(Pedido.StatusPedido.ENVIADO);
            pedido.setDataAtualizacao(LocalDateTime.now());
            pedidoRepository.save(pedido);
        }
    }

    /**
     * Versão de criação de pedido que já recebe os medicamentos extraídos
     * da receita e cria automaticamente os agendamentos de recompra.
     */
    @Transactional
    public PedidoResponseDTO criarPedidoComAgendamentos(
            PedidoRequestDTO pedidoDTO,
            List<MedicamentoExtraidoDTO.MedicamentoItem> itensExtraidos
    ) {
        // Reaproveita toda a lógica de criação do pedido normal
        PedidoResponseDTO responseDTO = criarPedido(pedidoDTO);

        // Busca o pedido recém-criado (já vem no DTO, mas pegamos do banco para garantir consistência)
        Pedido pedido = pedidoRepository.findById(responseDTO.getId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado após criação"));

        // Cria os agendamentos de recompra com base nos itens extraídos
        finalizarPedido(pedido, itensExtraidos);

        return responseDTO;
    }

    /**
     * Lógica para salvar automaticamente os agendamentos de recompra
     * no momento da compra.
     */
    @Transactional
    public void finalizarPedido(Pedido pedido, List<MedicamentoExtraidoDTO.MedicamentoItem> itensExtraidos) {
        if (itensExtraidos == null || itensExtraidos.isEmpty()) {
            return;
        }

        for (MedicamentoExtraidoDTO.MedicamentoItem item : itensExtraidos) {
            if (item.getDiasDuracao() != null && item.getProdutoId() != null) {
                AgendamentoRecompra agendamento = new AgendamentoRecompra();
                agendamento.setUsuario(pedido.getUsuario());
                agendamento.setProduto(
                        produtoRepository.findById(item.getProdutoId())
                                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + item.getProdutoId()))
                );
                agendamento.setPedido(pedido);
                agendamento.setPosologiaTexto(item.getPosologia());
                agendamento.setDiasDuracao(item.getDiasDuracao());
                agendamento.setStatus("PENDENTE");

                // Calcula a data: Hoje + Dias de Duração - 3 dias de antecedência
                agendamento.setDataProximaNotificacao(
                        LocalDateTime.now().plusDays(item.getDiasDuracao()).minusDays(3)
                );

                agendamentoRecompraRepository.save(agendamento);
            }
        }
    }

    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        return new PedidoResponseDTO(pedido);
    }

    public List<PedidoResponseDTO> buscarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<PedidoResponseDTO> buscarPorFarmacia(Long farmaciaId) {
        return pedidoRepository.findByFarmaciaId(farmaciaId)
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<Pedido> findPedidosComItensParaWellness(Long usuarioId) {
        return pedidoRepository.findPedidosComItensByUsuario(usuarioId);
    }

    public PedidoResponseDTO buscarPorCodigo(String codigo) {
        Pedido pedido = pedidoRepository.findByCodigoPedidoWithRastreio(codigo);
        if (pedido == null) {
            throw new RuntimeException("Pedido não encontrado com código: " + codigo);
        }
        return new PedidoResponseDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, String status) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        try {
            pedido.setStatus(Pedido.StatusPedido.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + status);
        }

        pedido.setDataAtualizacao(LocalDateTime.now());

        if (status.equals("ENVIADO") && pedido.getRastreio() == null) {
            Rastreio rastreio = new Rastreio(pedido);
            rastreio.setTransportadora("ClickFarma Express");
            rastreio.setDataEnvio(LocalDateTime.now());
            rastreio.setDataPrevisaoEntrega(LocalDateTime.now().plusDays(5));
            rastreio.setStatus("EM_TRANSITO");

            rastreioRepository.save(rastreio);
        }

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        emailNotificationService.enviarAtualizacaoStatusPedido(pedidoAtualizado.getUsuario(), pedidoAtualizado);
        return new PedidoResponseDTO(pedidoAtualizado);
    }

    @Transactional
    public RastreioResponseDTO rastrearPedido(String codigoPedido) {
        Pedido pedido = pedidoRepository.findByCodigoPedidoWithRastreio(codigoPedido);
        if (pedido == null) {
            throw new RuntimeException("Pedido não encontrado");
        }

        if (pedido.getRastreio() == null) {
            throw new RuntimeException("Pedido ainda não foi enviado");
        }

        return new RastreioResponseDTO(pedido.getRastreio());
    }

    @Transactional
    public RastreioResponseDTO atualizarRastreio(Long pedidoId, String localizacao, String status) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getRastreio() == null) {
            throw new RuntimeException("Pedido não possui rastreio");
        }

        Rastreio rastreio = pedido.getRastreio();
        rastreio.setUltimaLocalizacao(localizacao);
        rastreio.setStatus(status);
        rastreio.setUltimaAtualizacao(LocalDateTime.now());

        if (status.equals("ENTREGUE")) {
            rastreio.setDataEntregaReal(LocalDateTime.now());
            pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
        }

        rastreioRepository.save(rastreio);
        pedidoRepository.save(pedido);

        return new RastreioResponseDTO(rastreio);
    }

    public List<PedidoResponseDTO> buscarPorStatus(String status) {
        try {
            Pedido.StatusPedido statusEnum = Pedido.StatusPedido.valueOf(status);
            return pedidoRepository.findByStatus(statusEnum)
                    .stream()
                    .map(PedidoResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + status);
        }
    }

    public List<PedidoResponseDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.findByDataPedidoBetween(inicio, fim)
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<PedidoResponseDTO> buscarRecentes() {
        return pedidoRepository.findTop10ByOrderByDataPedidoDesc()
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatus() == Pedido.StatusPedido.ENTREGUE) {
            throw new RuntimeException("Pedido já entregue não pode ser cancelado");
        }

        if (pedido.getStatus() == Pedido.StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido já está cancelado");
        }

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoque(produto.getEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        pedido.setStatus(Pedido.StatusPedido.CANCELADO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoResponseDTO aceitarCorrida(Long pedidoId, Long motoboyId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        if (pedido.getMotoboy() != null) {
            throw new RuntimeException("Esta corrida já foi aceita por outro motoboy.");
        }
        
        com.clickfarma.backend.model.Motoboy motoboy = new com.clickfarma.backend.model.Motoboy();
        motoboy.setId(motoboyId); // Lazy load reference
        pedido.setMotoboy(motoboy);
        pedido.setStatus(Pedido.StatusPedido.ENVIADO); // Ou criar um status A_CAMINHO_DA_FARMACIA
        pedido.setDataAtualizacao(LocalDateTime.now());
        
        Pedido atualizado = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(atualizado);
    }

    @Transactional
    public PedidoResponseDTO confirmarRetirada(Long pedidoId, Long motoboyId, String codigoRetirada) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        if (pedido.getMotoboy() == null || !pedido.getMotoboy().getId().equals(motoboyId)) {
            throw new RuntimeException("Motoboy não autorizado para esta corrida.");
        }
        
        if (!pedido.getCodigoRetirada().equals(codigoRetirada)) {
            throw new RuntimeException("Código de retirada inválido.");
        }
        
        pedido.setStatus(Pedido.StatusPedido.EM_TRANSITO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        
        // Registrar localização atual no rastreio se aplicável
        if (pedido.getRastreio() != null) {
            atualizarRastreio(pedidoId, "Saiu da Farmácia", "EM_TRANSITO");
        }
        
        emailNotificationService.enviarAtualizacaoStatusPedido(pedido.getUsuario(), pedido);
        System.out.println("[WHATSAPP MOCK] Notificação enviada para Cliente: Seu pedido saiu para entrega!");
        
        return new PedidoResponseDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO confirmarEntrega(Long pedidoId, Long motoboyId, String codigoEntrega) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        if (pedido.getMotoboy() == null || !pedido.getMotoboy().getId().equals(motoboyId)) {
            throw new RuntimeException("Motoboy não autorizado para esta corrida.");
        }
        
        if (!pedido.getCodigoEntrega().equals(codigoEntrega)) {
            throw new RuntimeException("Código de entrega inválido.");
        }
        
        pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
        pedido.setDataAtualizacao(LocalDateTime.now());
        
        if (pedido.getRastreio() != null) {
            atualizarRastreio(pedidoId, "Entregue ao Cliente", "ENTREGUE");
        }
        
        // Verifica atraso
        java.time.Duration duracao = java.time.Duration.between(pedido.getDataPedido(), LocalDateTime.now());
        if (duracao.toMinutes() > 30) {
            com.clickfarma.backend.model.Motoboy mb = pedido.getMotoboy();
            if (mb != null && mb.getId() != null) {
                com.clickfarma.backend.model.Motoboy motoboyFromDb = motoboyRepository.findById(mb.getId()).orElse(null);
                if (motoboyFromDb != null) {
                    motoboyFromDb.setAtrasos(motoboyFromDb.getAtrasos() + 1);
                    motoboyRepository.save(motoboyFromDb);
                    System.out.println("[SISTEMA] Motoboy " + motoboyFromDb.getNome() + " penalizado por atraso (Total: " + motoboyFromDb.getAtrasos() + ").");
                }
            }
        }

        // Aciona o split de pagamentos (Simulado)
        System.out.println("[MERCADO PAGO MOCK] PIX enviado para Farmácia e Motoboy da entrega " + pedido.getId());
        emailNotificationService.enviarAtualizacaoStatusPedido(pedido.getUsuario(), pedido);
        System.out.println("[WHATSAPP MOCK] Notificação enviada para Cliente: Pedido Entregue com Sucesso!");
        
        return new PedidoResponseDTO(pedidoRepository.save(pedido));
    }

    public Map<String, Object> gerarRelatorio(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null) {
            inicio = LocalDateTime.now().minusMonths(1);
        }
        if (fim == null) {
            fim = LocalDateTime.now();
        }

        List<Pedido> pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim);

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("periodo", inicio + " até " + fim);
        relatorio.put("totalPedidos", pedidos.size());

        BigDecimal valorTotal = pedidos.stream()
                .map(Pedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        relatorio.put("valorTotal", valorTotal);

        Map<String, Long> contagemStatus = pedidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus() != null ? p.getStatus().name() : "SEM_STATUS",
                        Collectors.counting()
                ));
        relatorio.put("contagemStatus", contagemStatus);

        if (!pedidos.isEmpty()) {
            BigDecimal media = valorTotal.divide(BigDecimal.valueOf(pedidos.size()), 2, BigDecimal.ROUND_HALF_UP);
            relatorio.put("mediaValor", media);
        }

        return relatorio;
    }
}
