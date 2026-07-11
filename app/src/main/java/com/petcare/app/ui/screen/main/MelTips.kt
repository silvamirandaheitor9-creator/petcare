package com.petcare.app.ui.screen.main

import java.util.Calendar

/** Dica exibida no card da Mel na aba Início. */
data class MelTip(
    /** Espécie alvo: cachorro | gato | pássaro | peixe | réptil | roedor | geral */
    val species: String,
    val text: String,
)

/**
 * Banco de dicas da Mel — 100% factualmente corretas (SPEC seção 7).
 * Fontes: WSAVA (World Small Animal Veterinary Association), CFMV, AAHA,
 * estudos veterinários publicados e guias de cuidado animal consagrados.
 * Mínimo 30 por espécie principal (cachorro/gato), ≥10 para as demais.
 */
object MelTips {

    val all: List<MelTip> = buildList {

        // ── CACHORRO (32 dicas) ────────────────────────────────────────────────
        addAll(listOf(
            MelTip("cachorro", "A vacina V8 ou V10 protege contra cinomose, parvovírus e leptospirose. O esquema começa às 6–8 semanas e deve ser reforçado anualmente."),
            MelTip("cachorro", "A vacina antirrábica é obrigatória por lei em muitos estados brasileiros e precisa ser reforçada todo ano."),
            MelTip("cachorro", "Cães precisam de água fresca disponível 24 h por dia — a desidratação pode ser fatal em poucas horas."),
            MelTip("cachorro", "Para testar desidratação: levante levemente a pele do pescoço. Se demorar a voltar ao lugar, procure o veterinário."),
            MelTip("cachorro", "Chocolate contém teobromina, que é tóxica para cães e pode causar arritmia cardíaca e convulsões."),
            MelTip("cachorro", "Uvas e passas podem causar insuficiência renal aguda em cães — mesmo pequenas quantidades são perigosas."),
            MelTip("cachorro", "Cebola e alho (crus, cozidos ou em pó) destroem os glóbulos vermelhos dos cães, causando anemia hemolítica."),
            MelTip("cachorro", "Vermifugação deve ser feita a cada 3–6 meses em cães adultos. Filhotes seguem protocolo mensal nas primeiras semanas."),
            MelTip("cachorro", "A castração em fêmeas elimina o risco de piometra (infecção uterina) e reduz o risco de tumor mamário se feita antes do 2.º cio."),
            MelTip("cachorro", "Cães machos castrados têm risco reduzido de hiperplasia prostática benigna e tumor perianal."),
            MelTip("cachorro", "A erliquiose canina (doença do carrapato) pode causar anemia grave — use preventivo antiparasitário mensal regularmente."),
            MelTip("cachorro", "A leishmaniose visceral é transmitida pelo mosquito-palha e não tem cura definitiva — coleiras inseticidas ajudam a prevenir."),
            MelTip("cachorro", "Filhotes entre 8 e 16 semanas não devem ter contato com cães de saúde desconhecida antes de completar o esquema vacinal."),
            MelTip("cachorro", "Dentes de cães devem ser escovados pelo menos 3 vezes por semana para prevenir tártaro e doenças periodontais."),
            MelTip("cachorro", "Cães nunca devem ser deixados em carros fechados — em dias quentes, a temperatura interna pode matar em menos de 10 minutos."),
            MelTip("cachorro", "Obesidade em cães está associada a redução de até 2 anos na expectativa de vida e maior risco de diabetes e artrite."),
            MelTip("cachorro", "Cães idosos (acima de 7 anos em raças médias/grandes) devem fazer exames de sangue anuais para monitorar função renal e hepática."),
            MelTip("cachorro", "A parvovírus canina pode sobreviver no ambiente por até 1 ano em condições favoráveis — a vacinação é a principal proteção."),
            MelTip("cachorro", "A cinomose (distemper) afeta pulmões, intestinos e sistema nervoso. Não há tratamento específico — a vacina é a única proteção eficaz."),
            MelTip("cachorro", "A temperatura corporal normal de um cão é entre 38 °C e 39,5 °C. Acima de 40 °C é emergência."),
            MelTip("cachorro", "Microchipagem facilita o reencontro em caso de fuga — o chip é lido por qualquer clínica veterinária ou canil público."),
            MelTip("cachorro", "Cães braquicéfalos (Bulldog, Pug, Shih Tzu) têm vias aéreas estreitas e não devem se exercitar intensamente no calor."),
            MelTip("cachorro", "Parasitas intestinais de cães como Toxocara canis podem ser transmitidos a humanos — higiene das mãos é fundamental."),
            MelTip("cachorro", "Ração com proteína animal como primeiro ingrediente na lista é nutricionalmente superior às que listam milho ou farinha de subprodutos primeiro."),
            MelTip("cachorro", "Cães que coçam as patas repetidamente podem ter dermatite alérgica alimentar ou ambiental — consulte o veterinário."),
            MelTip("cachorro", "A frequência cardíaca normal em cães varia de 60 bpm (raças grandes) a 160 bpm (raças toy) em repouso."),
            MelTip("cachorro", "Visita anual ao veterinário é o mínimo recomendado pela WSAVA para cães adultos saudáveis."),
            MelTip("cachorro", "Cães diabéticos precisam de alimentação em horários fixos e controle rigoroso de peso para estabilizar a glicemia."),
            MelTip("cachorro", "Fungos como a esporotricose (transmissível a humanos e gatos) podem ser contraídos por arranhões — evite contato com solos contaminados."),
            MelTip("cachorro", "Cães que ficam presos em correntes por longos períodos desenvolvem distúrbios comportamentais e problemas físicos graves."),
            MelTip("cachorro", "Banho com frequência adequada (a cada 15–30 dias, dependendo da pelagem) previne dermatites e infecções de pele."),
            MelTip("cachorro", "A giardia é um parasita intestinal comum em filhotes e pode causar diarreia persistente — diagnóstico requer exame de fezes."),
        ))

        // ── GATO (33 dicas) ──────────────────────────────────────────────────
        addAll(listOf(
            MelTip("gato", "Gatos são carnívoros obrigatórios — não podem produzir taurina, arginina e vitamina A por conta própria e precisam obtê-los da carne."),
            MelTip("gato", "A doença renal crônica é a principal causa de morte em gatos acima de 10 anos. Exames anuais de creatinina e SDMA permitem diagnóstico precoce."),
            MelTip("gato", "Gatos que comem somente ração seca precisam de acesso constante à água fresca — considere uma fonte de água para estimular o consumo."),
            MelTip("gato", "Lírios (Lilium spp.) são extremamente tóxicos para gatos: qualquer parte da planta, incluindo o pólen, pode causar insuficiência renal aguda fatal."),
            MelTip("gato", "Paracetamol (acetaminofeno) é letal para gatos mesmo em doses muito baixas — nunca medique seu gato com remédios humanos."),
            MelTip("gato", "A vacinação tríplice felina (rinotraqueíte, calicivírus, panleucopenia) deve ser iniciada às 8–9 semanas e reforçada anualmente."),
            MelTip("gato", "A FIV (imunodeficiência felina) é transmitida por mordidas profundas entre gatos. Castração e vida indoor reduzem muito o risco."),
            MelTip("gato", "A FeLV (leucemia felina) é transmitida por contato próximo com saliva, urina e fezes. Existe vacina preventiva recomendada para gatos com acesso externo."),
            MelTip("gato", "Gatos que param de comer por mais de 48 horas correm risco de lipidose hepática (fígado gorduroso), uma condição grave e potencialmente fatal."),
            MelTip("gato", "Gatos escondem sintomas de doença por instinto de sobrevivência — mudanças sutis no comportamento (isolamento, menos grooming) merecem atenção imediata."),
            MelTip("gato", "A toxoplasmose é transmitida pelas fezes de gatos infectados — gestantes devem evitar limpar a caixa de areia ou usar luvas e máscara."),
            MelTip("gato", "Gatos machos não castrados têm risco elevado de FLUTD (doença do trato urinário inferior felino), que pode ser fatal se causar obstrução."),
            MelTip("gato", "A obesidade felina aumenta o risco de diabetes mellitus tipo 2, artrite, hepatite gordurosa e reduz a expectativa de vida."),
            MelTip("gato", "Gatos idosos (acima de 10 anos) devem ter consultas veterinárias a cada 6 meses — doenças endócrinas e renais são mais frequentes."),
            MelTip("gato", "Gatos domésticos bem cuidados vivem em média 12–18 anos; gatos que vivem exclusivamente indoor tendem a viver mais que os de rua."),
            MelTip("gato", "A frequência cardíaca normal em gatos é de 140–220 bpm — valores fora desta faixa em repouso podem indicar problemas cardíacos."),
            MelTip("gato", "A temperatura corporal normal de gatos é de 38 °C a 39,2 °C. Hipotermia (< 37 °C) e febre (> 39,5 °C) são emergências."),
            MelTip("gato", "Gatos precisam de pelo menos 1 caixa de areia por gato, mais uma extra. Sujeira na caixa é a principal causa de eliminação inadequada."),
            MelTip("gato", "Arranhadores são essenciais: gatos marcam território com glândulas nas patas e precisam arranhar para manter as garras saudáveis."),
            MelTip("gato", "Gatos que vivem sozinhos podem desenvolver ansiedade — brinquedos interativos e tempo de brincadeira diário reduzem o estresse."),
            MelTip("gato", "A panleukopenia felina (parvovírus felino) pode matar um gatinho em horas após os primeiros sintomas — vacinação é crítica."),
            MelTip("gato", "Gatos produzem bolas de pelo (hairballs) ao se lamber — escovação semanal e suplementos de fibra ajudam a prevenir obstrução intestinal."),
            MelTip("gato", "Brigas entre gatos frequentemente causam abscessos que não aparecem imediatamente — qualquer ferida ou inchaço deve ser avaliado pelo vet."),
            MelTip("gato", "O ronronar dos gatos tem frequência de 25 a 150 Hz, que em estudos estimula a regeneração óssea e de tecidos moles."),
            MelTip("gato", "Gatos precisam de 16 horas de sono por dia em média — isso é fisiologicamente normal e não indica doença."),
            MelTip("gato", "Microchipagem é recomendada mesmo para gatos 100% indoor — portas e janelas abertas acidentalmente são mais comuns do que parece."),
            MelTip("gato", "Ração úmida (lata/sachê) contribui significativamente para a hidratação diária dos gatos, complementando a ração seca."),
            MelTip("gato", "Gatos com acesso externo têm exposição 3× maior a doenças infecciosas e parasitas comparado a gatos exclusivamente indoor."),
            MelTip("gato", "Dietas caseiras para gatos precisam ser formuladas por nutricionista veterinário — deficiências de taurina causam cardiomiopatia."),
            MelTip("gato", "A esporotricose (fungo) é uma zoonose frequente no Brasil, especialmente em gatos de rua. Use luvas ao manusear arranhões."),
            MelTip("gato", "Gatos com diabetes mellitus precisam de insulina injetável e monitoramento glicêmico regular — a dieta controlada em carboidratos auxilia o tratamento."),
            MelTip("gato", "Plantas como filodendro, pothos e difenbaquia são tóxicas para gatos e causam irritação na boca, salivação e vômito."),
            MelTip("gato", "Gatos machos castrados após os 6 meses têm risco menor de obstrução urinária, mas ainda devem ser monitorados para FLUTD."),
        ))

        // ── PÁSSARO (11 dicas) ───────────────────────────────────────────────
        addAll(listOf(
            MelTip("pássaro", "Panelas antiaderentes de PTFE (teflon) superaquecidas liberam gases invisíveis que são letais para aves em minutos — prefira inox ou cerâmica."),
            MelTip("pássaro", "Aves escondem sinais de doença por instinto; perda de peso, penas arrepiadas e letargia são emergências — procure veterinário de aves."),
            MelTip("pássaro", "Abacate, chocolate, cafeína, álcool e cebola são altamente tóxicos para pássaros e podem causar morte rapidamente."),
            MelTip("pássaro", "A suplementação de vitamina A é importante para aves alimentadas principalmente com sementes — deficiência causa infecções respiratórias frequentes."),
            MelTip("pássaro", "Aves precisam de 10–12 horas de escuridão por noite para dormir bem; privação de sono causa estresse e doenças imunológicas."),
            MelTip("pássaro", "Periquitos e calopsitas são aves sociais que sofrem com isolamento — considere adotar um par ou proporcionar interação diária."),
            MelTip("pássaro", "Banho periódico (tigela rasa com água ou spray) é essencial para a saúde das penas e do sistema respiratório das aves."),
            MelTip("pássaro", "Periquitos australianos vivem 5–10 anos; calopsitas, 15–20 anos; araras podem ultrapassar 60–70 anos — planeje o longo prazo."),
            MelTip("pássaro", "A temperatura ideal para a maioria das aves tropicais é entre 20 °C e 30 °C; correntes de ar frio são uma das principais causas de morte."),
            MelTip("pássaro", "Penas aparadas crescem de volta — o procedimento precisa ser repetido regularmente para manter a limitação de voo."),
            MelTip("pássaro", "Sprays domésticos (inseticidas, desodorante, perfume, limpadores) são tóxicos para o sistema respiratório sensível das aves."),
        ))

        // ── PEIXE (10 dicas) ─────────────────────────────────────────────────
        addAll(listOf(
            MelTip("peixe", "O ciclo do nitrogênio deve ser completado (2–4 semanas) antes de colocar peixes no aquário — amônia elevada é letal."),
            MelTip("peixe", "Troque 20–30% da água do aquário semanalmente para controlar nitrato e manter a qualidade da água estável."),
            MelTip("peixe", "Peixes betta (siameses) machos não devem conviver no mesmo aquário — brigas ocorrem até a morte de um dos dois."),
            MelTip("peixe", "Peixe-dourado (kinguio) produz muito dejeto e precisa de aquários grandes (mínimo 40 L por peixe) e filtração potente."),
            MelTip("peixe", "Variações bruscas de temperatura causam estresse imunológico grave em peixes — invista em aquecedor com termostato."),
            MelTip("peixe", "Alimentação em excesso polui a água rapidamente — ofereça somente o que os peixes consomem em 2–3 minutos."),
            MelTip("peixe", "Iluminação acima de 10–12 h/dia estimula crescimento excessivo de algas e prejudica o ritmo circadiano dos peixes."),
            MelTip("peixe", "Peixes de água doce e água salgada têm necessidades radicalmente diferentes de salinidade, pH e temperatura."),
            MelTip("peixe", "Quarentena de novos peixes por 2–4 semanas em aquário separado previne a introdução de parasitas e doenças no aquário principal."),
            MelTip("peixe", "Salinidade inadequada em aquários marinhos causa desequilíbrio osmótico que pode matar os peixes em horas."),
        ))

        // ── RÉPTIL (10 dicas) ────────────────────────────────────────────────
        addAll(listOf(
            MelTip("réptil", "Répteis são ectotérmicos: dependem de fontes externas de calor para regular a temperatura e digerir alimentos."),
            MelTip("réptil", "A radiação UVB é essencial para a síntese de vitamina D3 em répteis — sem ela, desenvolvem doença metabólica óssea (MBD) progressiva."),
            MelTip("réptil", "Tartarugas aquáticas precisam de área seca com iluminação UVB para se aquecer pelo menos algumas horas por dia."),
            MelTip("réptil", "A salmonela está presente na microbiota natural de répteis e pode ser transmitida a humanos — lave as mãos sempre após o manuseio."),
            MelTip("réptil", "Temperatura inadequada impede a digestão em répteis — alimento não digerido no estômago pode apodrecer e causar infecção fatal."),
            MelTip("réptil", "A muda de pele (ecdise) em serpentes deve ocorrer em peça única — muda fragmentada indica baixa umidade no terrário."),
            MelTip("réptil", "Barbaças (Pogona vitticeps) são onívoras e precisam de vegetais folhosos, insetos vivos e suplementação de cálcio regular."),
            MelTip("réptil", "Camaleões são extremamente sensíveis ao estresse — manuseio frequente, espelhos e outras causas de estresse podem ser letais."),
            MelTip("réptil", "Iguana verde adulta pode atingir 1,5–2 m e requerer um recinto de pelo menos 4 m² — pesquise bem antes de adotar."),
            MelTip("réptil", "Serpentes adultas geralmente se alimentam 1× por semana ou a cada 10 dias — overfeeding causa obesidade e problemas hepáticos."),
        ))

        // ── ROEDOR (11 dicas) ────────────────────────────────────────────────
        addAll(listOf(
            MelTip("roedor", "Ratos e camundongos domésticos são altamente sociais — mantê-los sozinhos causa depressão e comportamentos compulsivos."),
            MelTip("roedor", "Hamsters sírios (dourados) são solitários e territoriais — dois machos adultos em uma gaiola brigarão até a morte."),
            MelTip("roedor", "Cobaias (porquinhos da índia) não sintetizam vitamina C — precisam recebê-la diariamente via vegetais frescos ou suplemento."),
            MelTip("roedor", "Chinchilas têm pelagem densa que apodrece com umidade — o banho deve ser feito somente com areia de chinchila, nunca com água."),
            MelTip("roedor", "Hamsters são animais noturnos — sons e movimentos à noite na gaiola são completamente normais e não indicam problema."),
            MelTip("roedor", "A gaiola de roedores deve ser limpa pelo menos 1× por semana — amônia da urina em acúmulo causa doenças respiratórias."),
            MelTip("roedor", "Ratos domésticos vivem 2–3 anos; cobaias, 4–7 anos; chinchilas podem viver 10–15 anos — considere o compromisso de longo prazo."),
            MelTip("roedor", "Os dentes de roedores crescem continuamente ao longo de toda a vida — forneça itens para roer (madeira, blocos minerais) para desgastá-los."),
            MelTip("roedor", "Hamsters armazenam alimento nas bolsas jugais — é normal vê-los com as bochechas muito inchadas; não force a esvaziar."),
            MelTip("roedor", "Cobaias vocalizam ativamente para se comunicar — sons distintos (ronronar, piados, dentes batendo) têm significados diferentes."),
            MelTip("roedor", "Roedores são sensíveis a cheiros fortes — evite usar sprays, velas ou produtos de limpeza aromáticos próximo à gaiola."),
        ))
    }

    /**
     * Seleciona uma dica para o dia com base na espécie do primeiro pet registrado.
     * A seleção é determinística (baseada no dia do ano) — a dica não muda ao reabrir o app.
     */
    fun pickForDay(species: String?): MelTip {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val speciesKey = species?.lowercase()?.trim()
        val pool = if (!speciesKey.isNullOrEmpty()) {
            all.filter { it.species == speciesKey }.ifEmpty { all }
        } else {
            all
        }
        return pool[dayOfYear % pool.size]
    }
}
