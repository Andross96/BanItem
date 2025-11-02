# Changelog - MGT-BanItem

## [1.0.0] - 2024-11-02

### ✨ Nova Implementação - Port para NeoForge 1.21.1

#### Adicionado
- Estrutura completa do mod NeoForge com Gradle
- Sistema de configuração JSON (substitui YAML do Bukkit)
- Database de blacklist e whitelist por mundo
- API pública para verificação de itens banidos
- Sistema de event listeners adaptado para Forge:
  - BlockBreakEvent → BlockEvent.BreakEvent
  - PlayerDropItemEvent → ItemTossEvent
  - PlayerPickupItemEvent → EntityItemPickupEvent
  - PlayerInteractEvent → PlayerInteractEvent.RightClickItem
  - BlockPlaceEvent → BlockEvent.EntityPlaceEvent
  - EntityDamageByEntityEvent → AttackEntityEvent
  - InventoryOpenEvent → PlayerContainerEvent.Open
  - InventoryCloseEvent → PlayerContainerEvent.Close
- Sistema de comandos com Brigadier:
  - `/banitem help` ou `/bi help` - Ajuda
  - `/banitem reload` ou `/bi reload` - Recarregar configuração
  - `/banitem info` ou `/bi info` - Informações do mod
- Suporte a 16 ações principais:
  - BREAK, CLICK, CONSUME, CRAFT, DROP, DROPS
  - HOLD, INTERACT, INVENTORYCLICK, PICKUP, PLACE
  - TRANSFER, USE, WEAR, ATTACK, DELETE

#### Arquivos Criados
- `build.gradle` - Configuração Gradle com NeoForge plugin
- `gradle.properties` - Propriedades do projeto e versões
- `settings.gradle` - Configuração de repositórios
- `src/main/java/fr/andross/banitem/`
  - `ModMain.java` (140 linhas)
  - `BanConfig.java` (125 linhas)
  - `BanDatabase.java` (165 linhas)
  - `BanListener.java` (240 linhas)
  - `BanItemAPI.java` (170 linhas)
  - `BanUtils.java` (55 linhas)
  - `actions/BanAction.java` (105 linhas)
  - `actions/BanActionData.java` (80 linhas)
  - `actions/BanData.java` (30 linhas)
  - `actions/BanDataType.java` (45 linhas)
  - `commands/BanCommand.java` (130 linhas)
  - `items/BannedItem.java` (85 linhas)
- `src/main/resources/META-INF/neoforge.mods.toml`
- `src/main/resources/assets/mgt_banitem/pack.mcmeta`
- `mgt-banitem-example.json` - Exemplo de configuração
- `README.md` - Documentação completa
- `CONVERSION_SUMMARY.md` - Resumo técnico da conversão
- `TODO.md` - Lista de implementações futuras

**Total**: ~1,300 linhas de código Java

#### Mudanças em Relação ao Plugin Original

**Formato de Configuração**
- YAML → JSON para melhor integração com Forge
- Estrutura mantida para facilitar migração
- Suporte a wildcards (*) para itens e ações

**Sistema de Eventos**
- Adaptado de Bukkit Events para Forge Events
- Uso de @EventBusSubscriber e @SubscribeEvent
- Handlers estáticos para melhor performance

**Comandos**
- Bukkit CommandExecutor → Brigadier
- Melhor autocompleção e validação de argumentos
- Integração nativa com sistema de permissões do Forge

**Permissões**
- Usa operator level do Forge (level 2 para admin)
- Preparado para integração futura com mods de permissões

**Versionamento**
- NeoForge 21.1.211 (Minecraft 1.21.1)
- Java 21 (requisito do NeoForge)
- Gradle 8.x com Gradle Plugin Portal

#### Documentação
- README.md completo com instruções de uso
- Exemplo de configuração JSON
- Resumo técnico da conversão
- Lista detalhada de TODOs para expansão futura

#### Estrutura de Pastas
```
MGT-BanItem/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── README.md
├── CONVERSION_SUMMARY.md
├── TODO.md
├── CHANGELOG.md (este arquivo)
├── mgt-banitem-example.json
└── src/
    └── main/
        ├── java/fr/andross/banitem/
        │   ├── ModMain.java
        │   ├── BanConfig.java
        │   ├── BanDatabase.java
        │   ├── BanListener.java
        │   ├── BanItemAPI.java
        │   ├── BanUtils.java
        │   ├── actions/
        │   ├── commands/
        │   └── items/
        └── resources/
            ├── META-INF/neoforge.mods.toml
            └── assets/mgt_banitem/pack.mcmeta
```

#### Compatibilidade
- ✅ NeoForge 1.21.1 (versão 21.1.211)
- ✅ Minecraft 1.21.1
- ✅ Java 21+
- ❌ Bukkit/Spigot (mod exclusivo para Forge)

#### Notas de Desenvolvimento
- Estrutura base completa e funcional
- Pronto para testes em servidor
- Necessita implementação de handlers adicionais para todas as 30+ ações do plugin original
- Sistema de hooks para outros mods planejado para versões futuras

#### Créditos
- **Plugin Original**: BanItem por André Sustac
- **Conversão NeoForge**: GnomoMuitoLoco
- **Licença**: GPL-3.0

---

## Próximas Versões Planejadas

### [1.1.0] - TBD
- Implementação completa de todos os event handlers
- Sistema avançado de parsing de configuração
- Suporte completo a NBT/componentes

### [1.2.0] - TBD
- Sistema de hooks para WorldGuard e outros mods
- Comandos administrativos completos
- Sistema de permissões avançado

### [2.0.0] - TBD
- Interface gráfica de configuração (mod de cliente)
- Sistema de metrics e estatísticas
- Performance optimizations
- API documentada para desenvolvedores

---

**Primeira Conversão**: 2 de Novembro de 2024
**Status**: Estrutura base completa
**Versão Estável**: Sim (com funcionalidade básica)
