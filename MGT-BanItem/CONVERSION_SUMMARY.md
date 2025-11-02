# MGT-BanItem - Conversão Completa do Plugin para Mod NeoForge 1.21.1

## Resumo da Conversão

Este documento resume a conversão do plugin Bukkit/Spigot BanItem para o mod NeoForge MGT-BanItem.

### Arquitetura do Mod

```
MGT-BanItem/
├── build.gradle                    # Configuração Gradle com plugin NeoForge
├── gradle.properties               # Propriedades do mod e versão NeoForge
├── settings.gradle                 # Configuração de repositórios
├── mgt-banitem-example.json        # Exemplo de configuração
└── src/main/
    ├── java/fr/andross/banitem/
    │   ├── ModMain.java            # Classe principal do mod (@Mod)
    │   ├── BanConfig.java          # Gerenciador de configuração JSON
    │   ├── BanDatabase.java        # Database de blacklist/whitelist
    │   ├── BanListener.java        # Handler de eventos Forge
    │   ├── BanItemAPI.java         # API pública do mod
    │   ├── BanUtils.java           # Utilitários gerais
    │   ├── actions/
    │   │   ├── BanAction.java      # Enum de ações (break, place, use, etc.)
    │   │   ├── BanData.java        # Dados adicionais de ações
    │   │   ├── BanDataType.java    # Tipos de dados adicionais
    │   │   └── BanActionData.java  # Dados de configuração de ações
    │   ├── commands/
    │   │   └── BanCommand.java     # Comandos Brigadier (/banitem, /bi)
    │   └── items/
    │       └── BannedItem.java     # Representação de item banido
    └── resources/
        ├── META-INF/
        │   └── neoforge.mods.toml  # Metadados do mod NeoForge
        └── assets/mgt_banitem/
            └── pack.mcmeta         # Assets do mod

```

### Principais Mudanças da Conversão

#### 1. Sistema de Build
- **Antes**: Maven com Bukkit API
- **Depois**: Gradle com NeoForge Gradle Plugin (net.neoforged.gradle.userdev)
- Java 21 como target (requisito do NeoForge 1.21.1)

#### 2. Metadados do Mod
- **Antes**: plugin.yml (Bukkit)
- **Depois**: neoforge.mods.toml (NeoForge 21.1.211)
- Usa `modLoader="neoforge"` e `loaderVersion="[21.1.211]"`

#### 3. Configuração
- **Antes**: YAML (config.yml)
- **Depois**: JSON (mgt-banitem.json)
- Estrutura similar mantida para facilitar migração

#### 4. Sistema de Eventos
| Bukkit Event | NeoForge Event |
|--------------|----------------|
| BlockBreakEvent | BlockEvent.BreakEvent |
| PlayerDropItemEvent | ItemTossEvent |
| PlayerPickupItemEvent | EntityItemPickupEvent |
| PlayerInteractEvent | PlayerInteractEvent.RightClickItem |
| BlockPlaceEvent | BlockEvent.EntityPlaceEvent |
| EntityDamageByEntityEvent | AttackEntityEvent |
| InventoryOpenEvent | PlayerContainerEvent.Open |
| InventoryCloseEvent | PlayerContainerEvent.Close |

#### 5. Comandos
- **Antes**: Bukkit Command API com CommandExecutor
- **Depois**: Brigadier com CommandDispatcher
- Comandos: `/banitem` e alias `/bi`
- Subcomandos: help, reload, info

#### 6. Classes Principais

**ModMain.java**
- Ponto de entrada do mod com @Mod annotation
- Gerencia lifecycle: FMLCommonSetupEvent
- Inicializa BanConfig, BanDatabase, BanItemAPI e BanListener
- Singleton acessível via ModMain.getInstance()

**BanConfig.java**
- Carrega/salva configuração JSON
- Gerencia prefix, priority actions
- Suporte a recarregamento em runtime

**BanDatabase.java**
- Contém Blacklist e Whitelist
- Gerencia custom items e meta items
- Estrutura por mundo (dimension)

**BanListener.java**
- @EventBusSubscriber para registro automático
- Handlers estáticos para eventos Forge
- Verificação de banimento via BanItemAPI

**BanItemAPI.java**
- API pública para outros mods
- Métodos isBanned(), isBlacklisted(), isWhitelisted()
- Suporte a Player e World contexts

**BanCommand.java**
- Registro de comandos no RegisterCommandsEvent
- Implementação com Brigadier
- Permissões via source.hasPermission(level)

#### 7. Ações Implementadas

Convertidas do plugin original:
- BREAK - Quebrar blocos
- CLICK - Clicar com item
- CONSUME - Consumir item
- CRAFT - Craftar item
- DROP - Dropar item
- DROPS - Drops de blocos
- HOLD - Segurar item
- INTERACT - Interagir com blocos
- INVENTORYCLICK - Clicar no inventário
- PICKUP - Pegar item
- PLACE - Colocar bloco
- TRANSFER - Transferir entre inventários
- USE - Usar item (botão direito)
- WEAR - Vestir armadura
- ATTACK - Atacar com item
- DELETE - Deletar automaticamente

### Compatibilidade

**NeoForge**: 21.1.211  
**Minecraft**: 1.21.1  
**Java**: 21+

### Status da Implementação

✅ **Completo (Estrutura Base)**
- Sistema de build e empacotamento
- Configuração JSON
- Database de blacklist/whitelist
- API básica de verificação
- Event handlers básicos
- Sistema de comandos
- Documentação

🚧 **A Implementar (Funcionalidades Avançadas)**
- Parsing completo de wildcards na configuração
- Todos os 30+ event handlers do plugin original
- Suporte avançado a NBT/componentes
- Sistema de hooks para outros mods
- Scanners (wear, illegal stack)
- Metrics e update checker

### Como Testar

1. Instalar Java 21+
2. Clonar o repositório
3. Navegar para MGT-BanItem/
4. Executar: `./gradlew build`
5. JAR será gerado em `build/libs/`
6. Copiar para pasta `mods/` do servidor NeoForge 1.21.1
7. Configurar via `config/mgt-banitem.json`

### Exemplos de Uso

**Banir TNT em todos os mundos:**
```json
{
  "blacklist": {
    "*": {
      "tnt": {
        "*": {
          "message": "&cTNT is banned!"
        }
      }
    }
  }
}
```

**Banir espada de diamante apenas no Nether:**
```json
{
  "blacklist": {
    "minecraft:the_nether": {
      "diamond_sword": {
        "attack": {
          "message": "&cNo diamond swords in the Nether!"
        }
      }
    }
  }
}
```

### Próximos Passos

1. Implementar todos os event handlers restantes
2. Sistema completo de parsing de configuração
3. Testes extensivos em servidor
4. Performance optimization
5. Sistema de hooks para WorldGuard, etc.
6. Documentação de API para desenvolvedores

### Contribuindo

O código está estruturado para fácil extensão:
- Adicione novos BanAction no enum
- Implemente handlers em BanListener
- Adicione comandos em BanCommand
- Documente mudanças no README

### Créditos

- **Plugin Original**: BanItem por André Sustac
- **Conversão NeoForge**: GnomoMuitoLoco
- **Licença**: GPL-3.0

---

**Data da Conversão**: 2 de Novembro de 2025
**Versão do Mod**: 1.0.0
**Status**: Estrutura base completa, pronto para testes e expansão
