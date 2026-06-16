package com.clickfarma.backend.config;

import com.clickfarma.backend.model.Categoria;
import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.CategoriaRepository;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🌱 Iniciando a verificação e semeadura de dados no ClickFarma...");

        // 1. Criar usuário de farmácia padrão se não houver nenhuma
        Usuario farmaciaUser = seedPharmacyUser();

        // 2. Criar farmácia padrão se não houver nenhuma
        Farmacia farmacia = seedPharmacy(farmaciaUser);

        // 3. Semear categorias
        Categoria catMedicamentos = seedCategoria("Medicamentos", "Medicamentos de referência, genéricos e similares");
        Categoria catCosmeticos = seedCategoria("Cosméticos", "Dermocosméticos, maquiagem e cuidados com a pele");
        Categoria catHigiene = seedCategoria("Higiene", "Cuidados diários, higiene pessoal e corporal");
        Categoria catVitaminas = seedCategoria("Vitaminas", "Polivitamínicos, suplementos e minerais");
        Categoria catMaternidade = seedCategoria("Maternidade", "Cuidados com o bebê, gestantes e pós-parto");

        // 4. Semear produtos para cada categoria
        seedProdutosParaCategoria(catMedicamentos, getMedicamentosData(), farmacia);
        seedProdutosParaCategoria(catCosmeticos, getCosmeticosData(), farmacia);
        seedProdutosParaCategoria(catHigiene, getHigieneData(), farmacia);
        seedProdutosParaCategoria(catVitaminas, getVitaminasData(), farmacia);
        seedProdutosParaCategoria(catMaternidade, getMaternidadeData(), farmacia);

        System.out.println("✅ Processo de semeadura de dados concluído com sucesso!");
    }

    private Usuario seedPharmacyUser() {
        String email = "farmacia.central@clickfarma.com";
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        Usuario user = new Usuario();
        user.setNome("Farmácia Central Owner");
        user.setEmail(email);
        user.setSenha(passwordEncoder.encode("farmacia123click"));
        user.setRole("PHARMACY");
        user.setCpf("123.456.789-00");
        user.setTelefone("(11) 99999-9999");
        user.setCidade("São Paulo");
        user.setEstado("SP");
        user.setCep("01001-000");
        user.setLogradouro("Praça da Sé");
        user.setNumero("100");
        user.setBairro("Sé");
        user.setEndereco("Praça da Sé, 100, Sé, São Paulo - SP");
        user.setDataCadastro(LocalDateTime.now());

        System.out.println("👤 Criando usuário de farmácia padrão...");
        return usuarioRepository.save(user);
    }

    private Farmacia seedPharmacy(Usuario owner) {
        List<Farmacia> farmacias = farmaciaRepository.findAll();
        if (!farmacias.isEmpty()) {
            return farmacias.get(0);
        }

        Farmacia farmacia = new Farmacia();
        farmacia.setNome("Farmácia Central ClickFarma");
        farmacia.setCnpj("12.345.678/0001-90");
        farmacia.setCep("01001-000");
        farmacia.setLogradouro("Praça da Sé");
        farmacia.setNumero("100");
        farmacia.setBairro("Sé");
        farmacia.setCidade("São Paulo");
        farmacia.setEstado("SP");
        farmacia.setTelefone("(11) 3333-3333");
        farmacia.setEmail("contato@farmaciacentralcf.com");
        farmacia.setChavePix("12.345.678/0001-90");
        farmacia.setTipoChavePix("CNPJ");
        farmacia.setUsuario(owner);
        farmacia.setDataCadastro(LocalDateTime.now());
        farmacia.setFotoUrl("https://images.unsplash.com/photo-1586015555751-63bb77f4322a?w=400");

        System.out.println("🏥 Criando farmácia padrão...");
        return farmaciaRepository.save(farmacia);
    }

    private Categoria seedCategoria(String nome, String descricao) {
        Optional<Categoria> catOpt = categoriaRepository.findByNome(nome);
        if (catOpt.isPresent()) {
            return catOpt.get();
        }

        Categoria categoria = new Categoria(nome, descricao);
        System.out.println("🏷️ Criando categoria: " + nome);
        return categoriaRepository.save(categoria);
    }

    private void seedProdutosParaCategoria(Categoria categoria, List<ProductSeedData> dataList, Farmacia farmacia) {
        long currentCount = produtoRepository.countByCategoriaId(categoria.getId());
        if (currentCount >= 20) {
            System.out.println("📦 Categoria '" + categoria.getNome() + "' já possui " + currentCount + " produtos. Pulando semeadura.");
            return;
        }

        System.out.println("📦 Semeando produtos para a categoria '" + categoria.getNome() + "'...");
        for (ProductSeedData data : dataList) {
            // Verificar se o produto já existe pelo nome para evitar duplicatas dentro da mesma categoria
            if (produtoRepository.existsByNomeAndCategoriaId(data.nome, categoria.getId())) {
                continue;
            }

            Produto produto = new Produto();
            produto.setNome(data.nome);
            produto.setDescricaoBreve(data.descricaoBreve);
            produto.setDescricao(data.descricao);
            produto.setPreco(data.preco);
            produto.setEstoque(data.estoque);
            produto.setImageUrl(data.imageUrl);
            produto.setLaboratorio(data.laboratorio);
            produto.setPrincipioAtivo(data.principioAtivo);
            produto.setDosagem(data.dosagem);
            produto.setNecessitaReceita(data.necessitaReceita);
            produto.setEmPromocao(data.emPromocao);
            produto.setDescontoPercentual(data.descontoPercentual);
            produto.setCategoria(categoria);
            produto.setFarmacia(farmacia);
            produto.setDataCriacao(LocalDateTime.now());
            produto.setDataAtualizacao(LocalDateTime.now());

            produtoRepository.save(produto);
        }
    }

    // Estrutura auxiliar para os dados de sementes de produtos
    private static class ProductSeedData {
        String nome;
        String descricaoBreve;
        String descricao;
        BigDecimal preco;
        Integer estoque;
        String imageUrl;
        String laboratorio;
        String principioAtivo;
        String dosagem;
        Boolean necessitaReceita;
        Boolean emPromocao;
        BigDecimal descontoPercentual;

        ProductSeedData(String nome, String descBreve, String desc, double preco, int estoque, String imageUrl,
                           String laboratorio, String principioAtivo, String dosagem, boolean necessitaReceita,
                           boolean emPromocao, double descontoPercentual) {
            this.nome = nome;
            this.descricaoBreve = descBreve;
            this.descricao = desc;
            this.preco = new BigDecimal(preco).setScale(2, RoundingMode.HALF_UP);
            this.estoque = estoque;
            this.imageUrl = imageUrl;
            this.laboratorio = laboratorio;
            this.principioAtivo = principioAtivo;
            this.dosagem = dosagem;
            this.necessitaReceita = necessitaReceita;
            this.emPromocao = emPromocao;
            this.descontoPercentual = new BigDecimal(descontoPercentual).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private List<ProductSeedData> getMedicamentosData() {
        List<ProductSeedData> list = new ArrayList<>();
        list.add(new ProductSeedData("Dipirona Monoidratada Medley 500mg", "Analgésico e antipirético em gotas",
                "Indicada para o alívio de dor e febre de intensidade leve a moderada. Apresentação em frasco conta-gotas de 20ml.",
                8.90, 150, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "Medley", "Dipirona Monoidratada", "500mg/mL", false, false, 0.0));
        list.add(new ProductSeedData("Paracetamol EMS 750mg", "Analgésico potente contra febre e dor",
                "Indicado para dor de cabeça, dor muscular e redução da febre. Blister contendo 20 comprimidos de fácil ingestão.",
                12.50, 200, "https://images.unsplash.com/photo-1550572017-edd951b55104?w=400",
                "EMS", "Paracetamol", "750mg", false, true, 10.0));
        list.add(new ProductSeedData("Ibuprofeno Neo Química 600mg", "Anti-inflamatório e analgésico oral",
                "Utilizado no alívio de processos inflamatórios, dores articulares e musculares. Contém 20 comprimidos revestidos.",
                15.90, 120, "https://images.unsplash.com/photo-1607619056574-7b8d304a2723?w=400",
                "Neo Química", "Ibuprofeno", "600mg", false, false, 0.0));
        list.add(new ProductSeedData("Omeprazol Eurofarma 20mg", "Protetor gástrico e antiácido em cápsulas",
                "Indicado para tratamento de úlceras gástricas, refluxo gastroesofágico e azia. Contém 28 cápsulas gelatinosas duras.",
                18.70, 180, "https://images.unsplash.com/photo-1584017911766-d451b3d0e843?w=400",
                "Eurofarma", "Omeprazol", "20mg", false, true, 5.0));
        list.add(new ProductSeedData("Amoxicilina Germed 500mg", "Antibiótico de amplo espectro para infecções",
                "Utilizado para o tratamento de infecções bacterianas das vias aéreas e urinárias. Venda sob prescrição e retenção de receita.",
                29.90, 80, "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=400",
                "Germed", "Amoxicilina", "500mg", true, false, 0.0));
        list.add(new ProductSeedData("Losartana Potássica Ache 50mg", "Anti-hipertensivo para controle de pressão",
                "Indicado para o tratamento da hipertensão arterial essencial e proteção renal. Caixa com 30 comprimidos.",
                9.99, 300, "https://images.unsplash.com/photo-1628771065518-0d82f1598626?w=400",
                "Ache", "Losartana Potássica", "50mg", true, false, 0.0));
        list.add(new ProductSeedData("Dorflex Sanofi 36 Comprimidos", "Analgésico e relaxante muscular eficaz",
                "Ideal para dor muscular, dor nas costas e tensões diárias. Contém orfenadrina, dipirona e cafeína. Embalagem econômica.",
                22.90, 250, "https://images.unsplash.com/photo-1607619056574-7b8d304a2723?w=400",
                "Sanofi", "Dipirona + Orfenadrina + Cafeína", "300mg+35mg+50mg", false, true, 15.0));
        list.add(new ProductSeedData("Neosaldina Takeda 30 Drágeas", "Tratamento rápido contra dor de cabeça",
                "Indicado para dor de cabeça tensional e enxaqueca leve. Ação rápida e prolongada. Blister com 30 drágeas.",
                27.50, 220, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "Takeda", "Dipirona + Mucato de Isometepteno + Cafeína", "300mg+30mg+30mg", false, false, 0.0));
        list.add(new ProductSeedData("Buscopan Duo Boehringer", "Alívio rápido de cólicas abdominais fortes",
                "Combinação de butilbrometo de escopolamina e paracetamol para alívio de dor e espasmos abdominais. Caixa com 20 comprimidos.",
                24.90, 140, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "Boehringer", "Butilbrometo de Escopolamina + Paracetamol", "10mg + 500mg", false, false, 0.0));
        list.add(new ProductSeedData("Tylenol 750mg J&J 20 Comprimidos", "Analgésico suave e seguro para dor e febre",
                "Indicado para redução da febre e alívio de dores leves a moderadas. Seguro para estômagos sensíveis. Uso adulto.",
                21.00, 160, "https://images.unsplash.com/photo-1550572017-edd951b55104?w=400",
                "Johnson & Johnson", "Paracetamol", "750mg", false, true, 8.0));
        list.add(new ProductSeedData("Allegra Sanofi 120mg", "Anti-histamínico de 24 horas sem dar sono",
                "Combate os sintomas da rinite alérgica e urticária sem causar sonolência. Contém fexofenadina. Embalagem com 10 comprimidos.",
                39.90, 95, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "Sanofi", "Cloridrato de Fexofenadina", "120mg", false, false, 0.0));
        list.add(new ProductSeedData("Claritin Bayer 10mg", "Antialérgico eficaz com loratadina",
                "Para alívio rápido de coriza, espirros e coceira nasal decorrentes de alergias. Caixa com 12 comprimidos de uso diário.",
                26.80, 110, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "Bayer", "Loratadina", "10mg", false, true, 12.0));
        list.add(new ProductSeedData("Cimegripe Cimed 20 Cápsulas", "Combate completo aos sintomas da gripe",
                "Indicado para o alívio imediato da dor, febre e congestão nasal provocadas por gripes e resfriados comuns.",
                14.20, 310, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "Cimed", "Paracetamol + Maleato de Clorfeniramina + Fenilefrina", "400mg+4mg+4mg", false, false, 0.0));
        list.add(new ProductSeedData("Vick Pyrena Mel e Limão", "Chá analgésico e descongestionante quente",
                "Sachê em pó para preparo de bebida quente. Alivia febre, dor no corpo e congestão nasal. Sabor mel e limão.",
                4.50, 400, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "P&G", "Paracetamol + Cloridrato de Fenilefrina", "500mg+10mg", false, false, 0.0));
        list.add(new ProductSeedData("Aerolin Spray Inalador 100mcg", "Broncodilatador de ação rápida para asma",
                "Indicado para prevenção e alívio do broncoespasmo na asma brônquica e bronquite crônica. Aerossol com 200 doses.",
                33.40, 75, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "GSK", "Sulfato de Salbutamol", "100mcg/dose", true, false, 0.0));
        list.add(new ProductSeedData("Simeticona EMS Gotas 75mg/mL", "Antigases para alívio do desconforto",
                "Ação rápida no rompimento das bolhas de ar no estômago e intestino, eliminando cólicas e gases. Frasco de 15ml.",
                9.50, 240, "https://images.unsplash.com/photo-1584017911766-d451b3d0e843?w=400",
                "EMS", "Simeticona", "75mg/mL", false, true, 10.0));
        list.add(new ProductSeedData("Pantoprazol Medley 40mg", "Tratamento de gastrite e úlcera estomacal",
                "Reduz a acidez gástrica e auxilia na cicatrização de esofagite e gastrite. Caixa contendo 28 comprimidos gastroresistentes.",
                35.90, 130, "https://images.unsplash.com/photo-1584017911766-d451b3d0e843?w=400",
                "Medley", "Pantoprazol Sódico Sesquidratado", "40mg", true, false, 0.0));
        list.add(new ProductSeedData("Cetoprofeno Eurofarma 150mg", "Anti-inflamatório potente de liberação lenta",
                "Indicado para inflamações agudas nas articulações, tendinites e dores pós-operatórias. Caixa com 10 comprimidos bi-desidratados.",
                23.60, 90, "https://images.unsplash.com/photo-1607619056574-7b8d304a2723?w=400",
                "Eurofarma", "Cetoprofeno", "150mg", true, false, 0.0));
        list.add(new ProductSeedData("Loratadina Neo Química 10mg", "Antialérgico genérico de uso diário",
                "Alívio eficaz contra sintomas de alergias respiratórias e cutâneas sem provocar sonolência. Caixa com 12 comprimidos.",
                11.90, 280, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "Neo Química", "Loratadina", "10mg", false, true, 15.0));
        list.add(new ProductSeedData("Nimesulida EMS 100mg", "Anti-inflamatório e analgésico de ação rápida",
                "Indicado para dor de garganta, dor de dente, inflamações musculares e articulares. Caixa com 12 comprimidos.",
                13.40, 190, "https://images.unsplash.com/photo-1607619056574-7b8d304a2723?w=400",
                "EMS", "Nimesulida", "100mg", true, false, 0.0));
        return list;
    }

    private List<ProductSeedData> getCosmeticosData() {
        List<ProductSeedData> list = new ArrayList<>();
        list.add(new ProductSeedData("Protetor Solar Anthelios FPS 60", "Protetor solar facial toque seco",
                "La Roche-Posay Anthelios Hydraox oferece alta proteção UVA/UVB com textura fluida e acabamento invisível. Sem perfume.",
                79.90, 100, "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=400",
                "La Roche-Posay", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Gel de Limpeza Facial CeraVe", "Gel de limpeza hidratante para pele normal a seca",
                "Limpa profundamente sem agredir a barreira protetora da pele. Contém 3 ceramidas essenciais e ácido hialurônico. 454g.",
                95.00, 80, "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400",
                "CeraVe", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Hidratante Clinique Moisture Surge", "Hidratante facial gel-creme 100h",
                "Promove hidratação profunda por até 100 horas. Textura em gel refrescante e sem óleo. Indicado para todos os tipos de pele.",
                229.00, 45, "https://images.unsplash.com/photo-1608248597481-496100c8c836?w=400",
                "Clinique", null, null, false, true, 15.0));
        list.add(new ProductSeedData("Água Micelar L'Oréal Paris 200ml", "Solução de limpeza facial 5 em 1",
                "Limpa, demaquila, purifica, suaviza e reequilibra a pele do rosto sem enxágue. Fórmula não oleosa. Dermatologicamente testado.",
                24.90, 150, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400",
                "L'Oréal", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Sérum Vitamina C Garnier Matte", "Sérum booster antimarcas e antioxidante",
                "Garnier Uniform & Matte reduz visivelmente marcas em apenas 3 dias com alta concentração de Vitamina C pura.",
                54.90, 110, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400",
                "Garnier", null, null, false, true, 5.0));
        list.add(new ProductSeedData("Creme Cicaplast Baume B5", "Creme reparador multirreparador calmante",
                "Acelera a cicatrização e acalma áreas irritadas e ressecadas do corpo e rosto. Contém pantenol e madecassoside.",
                68.50, 90, "https://images.unsplash.com/photo-1608248597481-496100c8c836?w=400",
                "La Roche-Posay", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Base Líquida Matte Boca Rosa", "Base de alta cobertura e longa duração",
                "Fórmula resistente à água com efeito aveludado e uniforme. Disfarça imperfeições com durabilidade excelente.",
                49.90, 70, "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400",
                "Boca Rosa", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Batom Matte Superstay Maybelline", "Batom líquido matte de longa duração 24h",
                "A tecnologia Superstay garante fixação por 24 horas sem borrar ou transferir. Cor intensa de acabamento profissional.",
                74.90, 85, "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400",
                "Maybelline", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Corretivo Líquido Matte Tracta", "Corretivo de alta cobertura com secagem rápida",
                "Ideal para camuflar olheiras, manchas e imperfeições. Efeito matte confortável que não craquela. Livre de óleos.",
                32.00, 120, "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400",
                "Tracta", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Máscara de Cílios The Colossal", "Máscara para cílios com volume extremo",
                "Fórmula com colágeno que proporciona até 9x mais volume instantaneamente sem grumos. Versão lavável preta.",
                45.90, 130, "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400",
                "Maybelline", null, null, false, true, 8.0));
        list.add(new ProductSeedData("Shampoo Tresemmé Reconstrução", "Shampoo para cabelos danificados e quebradiços",
                "Enriquecido com proteína e aminoácidos para reconstruir a fibra capilar enfraquecida por processos químicos. 400ml.",
                21.50, 140, "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=400",
                "Tresemmé", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Condicionador Elseve Hidra 400ml", "Condicionador com ácido hialurônico",
                "Elseve Hidra Hialurônico retém a hidratação profunda nos fios de cabelo, combatendo o ressecamento e o frizz.",
                23.90, 160, "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=400",
                "L'Oréal", null, null, false, true, 5.0));
        list.add(new ProductSeedData("Máscara Capilar Morte Súbita", "Creme de hidratação profunda e nutrição",
                "Máscara reconstrutora Lola Cosmetics. Ideal para cabelos ressecados e sedentos por vitalidade. Pote de 450g.",
                38.90, 60, "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=400",
                "Lola Cosmetics", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Creme de Pentear Bio Extratus", "Creme antifrizz com queratina e tutano",
                "Protege a fibra capilar contra o calor de secadores e chapinhas, promovendo brilho, maciez e definição aos cabelos.",
                29.00, 110, "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=400",
                "Bio Extratus", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Óleo Capilar Extraordinário L'Oréal", "Óleo finalizador reconstrutor de pontas",
                "Fórmula leve que nutre sem pesar, proporcionando maciez, brilho extraordinário e proteção térmica.",
                48.90, 75, "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=400",
                "L'Oréal", null, null, false, true, 12.0));
        list.add(new ProductSeedData("Sabonete Facial Neutrogena Purifying", "Sabonete líquido purificante profunda",
                "Neutrogena Purifying Skin limpa os poros, remove a oleosidade excessiva e reduz cravos sem ressecar o rosto.",
                34.50, 115, "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400",
                "Neutrogena", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Tônico Adstringente Nivea 200ml", "Tônico para peles mistas a oleosas",
                "Remove impurezas e resíduos de maquiagem, controlando a oleosidade e preparando a pele para a hidratação diária.",
                28.90, 105, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400",
                "Nivea", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Gel Secativo Acne Neutrogena", "Gel de ação rápida no combate a espinhas",
                "Reduz visivelmente o tamanho e a vermelhidão das espinhas em até 8 horas. Fórmula rápida, transparente e discreta.",
                39.90, 95, "https://images.unsplash.com/photo-1608248597481-496100c8c836?w=400",
                "Neutrogena", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Sérum Revitalift L'Oréal", "Sérum anti-idade preenchedor 1.5% hialurônico",
                "Composto por ácido hialurônico puro, preenche linhas finas de expressão e suaviza rugas. Toque leve de absorção imediata.",
                119.00, 50, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400",
                "L'Oréal", null, null, false, true, 15.0));
        list.add(new ProductSeedData("Máscara Facial Argila Pura L'Oréal", "Máscara facial matificante e purificante",
                "Combina 3 argilas puras e extrato de eucalipto. Absorve o excesso de sebo e purifica a pele em 10 minutos.",
                44.90, 80, "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400",
                "L'Oréal", null, null, false, false, 0.0));
        return list;
    }

    private List<ProductSeedData> getHigieneData() {
        List<ProductSeedData> list = new ArrayList<>();
        list.add(new ProductSeedData("Sabonete em Barra Dove Original", "Sabonete hidratante com 1/4 de creme",
                "Limpa de forma suave e nutre a pele sensível do corpo, prevenindo o ressecamento. Caixa com 90g.",
                3.90, 500, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Dove", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Creme Dental Colgate Total 12", "Creme dental com proteção antibacteriana",
                "Previne cáries, tártaro, gengivite e proporciona hálito fresco por até 12 horas. Tubo contendo 90g.",
                6.50, 450, "https://images.unsplash.com/photo-1559599261-2a44bd76f54c?w=400",
                "Colgate", null, null, false, true, 5.0));
        list.add(new ProductSeedData("Escova de Dentes Oral-B Indicator", "Escova dental macia com cabo anatômico",
                "Cerdas azuis que desbotam indicando a hora de trocar a escova. Leve e prática para o dia a dia. Embalagem unitária.",
                8.90, 300, "https://images.unsplash.com/photo-1559599104-306693f25940?w=400",
                "Oral-B", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Fio Dental Essential Floss 50m", "Fio dental encerado resistente",
                "Desliza suavemente entre os dentes, removendo com eficiência restos de comida e a placa bacteriana de difícil acesso.",
                11.20, 280, "https://images.unsplash.com/photo-1559599261-2a44bd76f54c?w=400",
                "Johnson's", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Desodorante Rexona Clinical Men", "Antitranspirante roll-on de máxima proteção",
                "Combate o suor excessivo e o odor por até 96 horas. Fórmula suave que protege a pele da axila masculina.",
                22.50, 180, "https://images.unsplash.com/photo-1619451334792-150fd785ee74?w=400",
                "Rexona", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Enxaguante Bucal Listerine 500ml", "Enxaguante bucal sem álcool hortelã",
                "Elimina até 99.9% dos germes que causam mau hálito, placa bacteriana e gengivite. Sabor hortelã refrescante.",
                19.90, 200, "https://images.unsplash.com/photo-1559599261-2a44bd76f54c?w=400",
                "Listerine", null, null, false, true, 8.0));
        list.add(new ProductSeedData("Absorvente Sempre Livre Adapt", "Absorvente menstrual com abas suave",
                "Adapta-se ao corpo protegendo contra vazamentos. Canais antivazamento. Cobertura suave tipo algodão. 8 unidades.",
                5.90, 350, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Sempre Livre", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Protetor Diário Carefree 80un", "Protetor diário respirável e confortável",
                "Mantém a sensação de calcinha limpa e fresca todos os dias. Sem perfume, hipoalergênico e respirável. Caixa econômica.",
                18.50, 220, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Carefree", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Papel Higiênico Neve Folha Dupla", "Papel higiênico folha dupla de alta maciez",
                "Tecnologia Dermacare que preserva o pH da pele. Contém 12 rolos de 30 metros cada. Maciez incomparável.",
                24.90, 130, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Neve", null, null, false, true, 5.0));
        list.add(new ProductSeedData("Lenços Umedecidos Huggies 48un", "Lenços umedecidos de limpeza suave",
                "Composto por fibras naturais e aloe vera. Sem álcool etílico. Hipoalergênico e ideal para higiene diária de toda a família.",
                12.90, 260, "https://images.unsplash.com/photo-1544816155-12df9643f363?w=400",
                "Huggies", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Hastes Flexíveis Cotonetes J&J", "Hastes flexíveis com pontas de algodão puro",
                "Cotonetes originais com barreira de proteção no miolo da haste que não quebra. Caixa contendo 75 unidades.",
                4.20, 400, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Johnson & Johnson", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Sabonete Líquido Protex Aloe", "Sabonete líquido corporal antibacteriano",
                "Elimina 99.9% das bactérias. Fórmula com extrato de Aloe Vera que acalma e hidrata a pele sensível. Refil de 250ml.",
                10.80, 240, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Protex", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Xampu Antiqueda Medikas 200ml", "Xampu terapêutico antiqueda capilar",
                "Fortalece os bulbos capilares combatendo a queda crônica do cabelo. Sem parabenos. Ideal para uso contínuo.",
                42.90, 90, "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=400",
                "Medikas", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Sabonete Íntimo Dermacyd Femina", "Sabonete líquido íntimo de uso diário",
                "Equilibra o pH natural da região íntima, mantendo a proteção ácida e prevenindo odores. Frasco contendo 200ml.",
                21.90, 110, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Dermacyd", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Aparelho de Barbear Prestobarba 3", "Aparelho de barbear descartável 3 lâminas",
                "Cabeça móvel que se adapta aos contornos do rosto masculino. Fita lubrificante com aloe vera. Pacote com 2 unidades.",
                9.80, 310, "https://images.unsplash.com/photo-1619451334792-150fd785ee74?w=400",
                "Gillette", null, null, false, true, 15.0));
        list.add(new ProductSeedData("Carga Gillette Mach3 Sensitive", "Carga de reposição com 3 lâminas sensíveis",
                "Lâminas mais afiadas que cortam o pelo com menor irritação na pele sensível. Embalagem contendo 4 cartuchos.",
                45.90, 140, "https://images.unsplash.com/photo-1619451334792-150fd785ee74?w=400",
                "Gillette", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Espuma de Barbear Bozzano 200ml", "Espuma de barbear hidratação intensiva",
                "Abaixa o atrito da lâmina com a pele, acalmando o rosto contra irritações causadas pela lâmina. Toque refrescante.",
                15.40, 170, "https://images.unsplash.com/photo-1619451334792-150fd785ee74?w=400",
                "Bozzano", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Loção Pós-Barba Sensitive Nivea", "Loção refrescante sem álcool pós-barba",
                "Com camomila e vitamina E. Alivia instantaneamente a vermelhidão e o ardor da pele recém-barbeada. 100ml.",
                32.50, 85, "https://images.unsplash.com/photo-1619451334792-150fd785ee74?w=400",
                "Nivea Men", null, null, false, true, 8.0));
        list.add(new ProductSeedData("Talco Antisséptico Sport Granado", "Talco para os pés contra frieiras e odores",
                "Combate o suor excessivo nos pés e calçados, eliminando germes e bactérias causadoras do mau odor. Lata de 100g.",
                13.90, 210, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Granado", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Algodão em Disco Apolo 100un", "Discos de algodão macio para limpeza facial",
                "Dupla face: ideal para remoção de maquiagem e aplicação de cosméticos como tônicos. Não solta fiapos irritantes.",
                9.90, 330, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Apolo", null, null, false, true, 10.0));
        return list;
    }

    private List<ProductSeedData> getVitaminasData() {
        List<ProductSeedData> list = new ArrayList<>();
        list.add(new ProductSeedData("Vitamina C Redoxon 1g", "Vitamina C efervescente contra gripes",
                "Suplemento vitamínico indicado para fortalecimento da imunidade de forma rápida e prática. Tubo com 10 comprimidos efervescentes.",
                18.90, 320, "https://images.unsplash.com/photo-1616679911721-eff6eec18fcd?w=400",
                "Redoxon", "Vitamina C", "1g", false, false, 0.0));
        list.add(new ProductSeedData("Vitamina D Addera D3 7000 UI", "Suplemento de vitamina D em comprimidos",
                "Addera D3 auxilia na absorção do cálcio e fortalecimento ósseo e muscular. Caixa com 30 comprimidos de uso semanal.",
                49.90, 150, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Addera", "Vitamina D3", "7000 UI", false, true, 12.0));
        list.add(new ProductSeedData("Centrum Todo Dia Multivitamínico", "Polivitamínico completo de A a Zinco",
                "Composto por 24 micronutrientes para complementar a nutrição e dar energia no dia a dia. Pote contendo 60 comprimidos.",
                65.00, 180, "https://images.unsplash.com/photo-1616679911721-eff6eec18fcd?w=400",
                "Centrum", "Polivitamínico", "Completo", false, true, 15.0));
        list.add(new ProductSeedData("Lavitan A-Z 60 Drágeas", "Suplemento vitamínico mineral econômico",
                "Fornece energia física e mental, além de fortalecer a imunidade com ótimo custo-benefício. Caixa com 60 drágeas.",
                22.90, 300, "https://images.unsplash.com/photo-1616679911721-eff6eec18fcd?w=400",
                "Cimed", "Polivitamínico", "Standard", false, false, 0.0));
        list.add(new ProductSeedData("Vitamina E Sundown 400 UI", "Antioxidante potente contra radicais livres",
                "Cápsulas em gel de vitamina E natural de fácil absorção. Auxilia na saúde celular e cardiovascular. Frasco de 100 cápsulas.",
                89.90, 85, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Sundown", "Vitamina E", "400 UI", false, false, 0.0));
        list.add(new ProductSeedData("Cálcio + Vitamina D3 Oscal", "Prevenção e tratamento de osteoporose",
                "Suplemento composto por cálcio de ostra associado à vitamina D3. Caixa contendo 60 comprimidos mastigáveis sabor menta.",
                54.50, 110, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Sanofi", "Cálcio Carbonato + Vitamina D3", "500mg + 200 UI", false, true, 8.0));
        list.add(new ProductSeedData("Colágeno Hidrolisado Verisol", "Colágeno em pó sabor frutas vermelhas",
                "Auxilia na firmeza da pele e redução de rugas faciais finas. Pote de 200g. Fácil diluição em água ou sucos.",
                74.90, 100, "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400",
                "Sanavita", "Colágeno Hidrolisado", "Verisol", false, false, 0.0));
        list.add(new ProductSeedData("Ômega 3 Catarinense 1000mg", "Óleo de peixe purificado em cápsulas",
                "Rico em EPA e DHA. Auxilia na manutenção de níveis saudáveis de triglicerídeos. Caixa com 120 cápsulas gelatinosas.",
                39.90, 140, "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400",
                "Catarinense", "Óleo de Peixe", "1000mg", false, true, 10.0));
        list.add(new ProductSeedData("Cloreto de Magnésio PA Apis Flora", "Cloreto de magnésio em sachê puro",
                "Suplemento mineral que auxilia no bom funcionamento muscular e ósseo. Pacote com 33g para preparo em água.",
                6.50, 420, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Apis Flora", "Cloreto de Magnésio", "PA", false, false, 0.0));
        list.add(new ProductSeedData("Bepantriz Pomada Cimed", "Pomada protetora para assaduras e pele",
                "Rica em dexpantenol (Vitamina B5). Estimula a cicatrização e regeneração natural da pele machucada ou assada. Tubo com 30g.",
                13.40, 270, "https://images.unsplash.com/photo-1608248597481-496100c8c836?w=400",
                "Cimed", "Dexpantenol", "50mg/g", false, true, 5.0));
        list.add(new ProductSeedData("Complexo B EMS 100 Comprimidos", "Suplemento do complexo B completo",
                "Contém vitaminas B1, B2, B3, B5, B6 e B12. Auxilia no metabolismo energético e saúde do sistema nervoso central.",
                18.20, 230, "https://images.unsplash.com/photo-1616679911721-eff6eec18fcd?w=400",
                "EMS", "Vitaminas do Complexo B", "Standard", false, false, 0.0));
        list.add(new ProductSeedData("Geleia Real Liofilizada 30 cáps", "Energético natural e protetor imunitário",
                "Superalimento em cápsulas que melhora a disposição física, o raciocínio mental e estimula o sistema imunitário.",
                48.90, 75, "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400",
                "Apis Flora", "Geleia Real Liofilizada", "100mg", false, true, 10.0));
        list.add(new ProductSeedData("Melatonina Neo Química Gotas", "Hormônio do sono em gotas sublinguais",
                "Auxilia na indução rápida do sono e melhora da qualidade do descanso noturno. Sabor menta. Frasco com 30ml.",
                29.90, 160, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Neo Química", "Melatonina", "0.21mg/gota", false, false, 0.0));
        list.add(new ProductSeedData("Zinco Quelato Nutriex 30mg", "Fortalecimento imunológico e antioxidante",
                "O zinco quelato possui excelente absorção gástrica, ideal para a saúde de cabelos, unhas e imunidade. 30 cápsulas.",
                24.50, 190, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Nutriex", "Zinco Quelato", "30mg", false, true, 8.0));
        list.add(new ProductSeedData("Cranberry Nutriex 500mg", "Prevenção natural de infecções urinárias",
                "Suplemento natural rico em proantocianidinas que impedem a adesão de bactérias na bexiga. Pote com 60 cápsulas.",
                39.90, 115, "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400",
                "Nutriex", "Cranberry", "500mg", false, false, 0.0));
        list.add(new ProductSeedData("Imunomult Multi Centrum", "Vitaminas e minerais para reforço imune",
                "Contém altas doses de vitamina C, D e Zinco para fortalecer as defesas naturais do corpo diariamente. 60 cápsulas.",
                59.90, 95, "https://images.unsplash.com/photo-1616679911721-eff6eec18fcd?w=400",
                "Centrum", "Polivitamínico", "Imunomult", false, true, 15.0));
        list.add(new ProductSeedData("Lavitan Cabelos e Unhas Cimed", "Suplemento fortalecedor capilar",
                "Combina biotina, zinco e cromo para fortalecer a raiz dos cabelos e evitar a quebra das unhas. Caixa com 30 cápsulas.",
                27.50, 240, "https://images.unsplash.com/photo-1616679911721-eff6eec18fcd?w=400",
                "Cimed", "Biotina + Zinco", "Standard", false, false, 0.0));
        list.add(new ProductSeedData("Vitamina B12 Cronobê Biolab", "Prevenção de anemia e fadiga",
                "Indicada para reposição em dietas vegetarianas e em pacientes idosos com baixa absorção. Caixa com 4 ampolas orais.",
                32.60, 105, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Biolab", "Cianocobalamina", "1000mcg", false, false, 0.0));
        list.add(new ProductSeedData("Levedo de Cerveja Catarinense", "Fonte de vitaminas do complexo B natural",
                "Auxilia no trânsito intestinal e na saúde de peles acnéicas e unhas quebradiças. Frasco com 400 comprimidos.",
                19.90, 130, "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=400",
                "Catarinense", "Levedo de Cerveja", "500mg", false, true, 10.0));
        list.add(new ProductSeedData("Coenzima Q10 Sanavita 100mg", "Energia celular e proteção cardíaca",
                "Aumenta a disposição física, melhora o rendimento nos treinos e combate o envelhecimento precoce. 60 cápsulas.",
                69.90, 70, "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400",
                "Sanavita", "Coenzima Q10", "100mg", false, false, 0.0));
        return list;
    }

    private List<ProductSeedData> getMaternidadeData() {
        List<ProductSeedData> list = new ArrayList<>();
        list.add(new ProductSeedData("Fralda Pampers Confort Sec G", "Fralda descartável ultra absorvente",
                "Mantém o bebê sequinho por até 12 horas. Barreiras antivazamento confortáveis. Embalagem econômica com 60 fraldas.",
                79.90, 200, "https://images.unsplash.com/photo-1544816155-12df9643f363?w=400",
                "Pampers", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Lenços Pampers Sensitive 56un", "Lenços umedecidos hipoalergênicos",
                "Ideais para a pele sensível do recém-nascido. Sem perfume, sem álcool e dermatologicamente testados.",
                15.90, 250, "https://images.unsplash.com/photo-1544816155-12df9643f363?w=400",
                "Pampers", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Pomada Hipoglós Amêndoas 40g", "Pomada para prevenção de assaduras",
                "Contém óleo de amêndoas e vitaminas A e E. Forma uma barreira protetora de fácil remoção e perfume suave.",
                18.50, 180, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Hipoglós", null, null, false, true, 5.0));
        list.add(new ProductSeedData("Pomada Desitin Máxima Proteção", "Pomada barreira contra assaduras forte",
                "Contém 40% de óxido de zinco, indicada para tratar assaduras severas desde a primeira aplicação. Tubo de 57g.",
                34.90, 130, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Desitin", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Shampoo Bebê Johnson's Original", "Shampoo suave para bebês 'Chega de Lágrimas'",
                "Limpa suavemente os cabelos finos do bebê sem arder os olhos. pH balanceado e livre de parabenos. Frasco de 400ml.",
                22.90, 140, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Johnson's", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Sabonete Líquido Huggies Bebê", "Sabonete líquido infantil extra suave",
                "Para uso da cabeça aos pés. Limpeza dermatologicamente testada que não agride a barreira cutânea do bebê. 200ml.",
                14.50, 160, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Huggies", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Condicionador Johnson's Baby 200ml", "Condicionador desembaraçante infantil",
                "Deixa os cabelos do bebê macios, fáceis de pentear e com o cheirinho clássico e suave característico da marca.",
                16.90, 150, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Johnson's", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Creme Anti-estrias Mustela", "Creme preventivo de estrias para gestantes",
                "Favorece a elasticidade da pele da gestante prevenindo o surgimento de estrias. Hipoalergênico. Tubo com 150ml.",
                119.90, 50, "https://images.unsplash.com/photo-1608248597481-496100c8c836?w=400",
                "Mustela", null, null, false, true, 15.0));
        list.add(new ProductSeedData("Protetor de Seio Philips Avent", "Absorventes térmicos descartáveis para seios",
                "Discretos e ultra absorventes para evitar vazamento de leite sob a roupa. Embalagem contendo 30 unidades.",
                45.90, 95, "https://images.unsplash.com/photo-1522850959516-58f958dba60f?w=400",
                "Avent", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Absorvente para Seios Johnson's", "Absorventes higiênicos para lactantes",
                "Desenvolvidos para absorver o excesso de leite materno entre as mamadas, mantendo a região seca e saudável. 30un.",
                24.50, 110, "https://images.unsplash.com/photo-1522850959516-58f958dba60f?w=400",
                "Johnson's", null, null, false, true, 8.0));
        list.add(new ProductSeedData("Óleo de Amêndoas Paixão 200ml", "Óleo hidratante corporal contra estrias",
                "Ideal para hidratação intensiva de gestantes, prevenindo o estiramento doloroso da pele do abdômen.",
                18.90, 120, "https://images.unsplash.com/photo-1608248597481-496100c8c836?w=400",
                "Paixão", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Chupeta Avent Soothie 0-3m", "Chupeta calmante de silicone peça única",
                "Respeita o desenvolvimento correto do palato, gengivas e dentes do bebê. Silicone de uso hospitalar. 2 unidades.",
                39.90, 80, "https://images.unsplash.com/photo-1522850959516-58f958dba60f?w=400",
                "Avent", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Mamadeira Avent Pétala 260ml", "Mamadeira com bico pétala anticólica",
                "Facilita a amamentação mista (peito e mamadeira) devido ao formato do bico largo que simula o seio materno.",
                89.90, 75, "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=400",
                "Avent", null, null, false, true, 10.0));
        list.add(new ProductSeedData("Algodão Quadrado Cremer 150g", "Algodão macio especial para bebês",
                "Ideal para higiene suave do bebê e remoção de pomadas. 100% puro algodão hidrófilo de alta absorção.",
                12.90, 190, "https://images.unsplash.com/photo-1607006342460-7a97f82be3fd?w=400",
                "Cremer", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Talco Barla Bebê Antisséptico", "Talco protetor contra umidade e assaduras",
                "Absorve a umidade das dobras de pele do bebê, deixando-o fresco, sequinho e protegido contra brotuejas. 100g.",
                9.50, 220, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Barla", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Pomada Lansinoh Lanolina HPA", "Pomada protetora para mamilos rachados",
                "100% natural e hipoalergênica. Alivia a dor e regenera mamilos machucados pela amamentação. Não precisa retirar antes de mamar.",
                59.90, 65, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Lansinoh", null, null, false, true, 5.0));
        list.add(new ProductSeedData("Sabonete de Glicerina Granado", "Sabonete líquido hipoalergênico bebê",
                "Fórmula suave com glicerina vegetal pura. Limpa sem ressecar e respeita o pH da pele do bebê. Frasco pump de 250ml.",
                21.90, 160, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Granado", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Loção Hidratante Mustela Bebê", "Hidratante corporal infantil diário",
                "Mustela Hidra Bebê com abacate de cultivo orgânico. Hidratação imediata e duradoura. Frasco com 300ml.",
                69.90, 85, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Mustela", null, null, false, true, 8.0));
        list.add(new ProductSeedData("Colônia Mamãe e Bebê Natura", "Água de colônia infantil sem álcool 100ml",
                "Cheirinho clássico de bebê. Fórmula vegana com 99% de ingredientes naturais. Não agride a pele delicada do bebê.",
                49.90, 110, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400",
                "Natura", null, null, false, false, 0.0));
        list.add(new ProductSeedData("Termômetro Clínico G-Tech", "Termômetro digital ponta flexível à prova d'água",
                "Leitura rápida da temperatura corporal em até 1 minuto, com bip sonoro de alerta de febre e memória da última medição.",
                19.90, 140, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
                "G-Tech", null, null, false, true, 10.0));
        return list;
    }
}
