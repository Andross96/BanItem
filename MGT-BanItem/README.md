# MGT-BanItem (NeoForge)

Scaffold inicial do mod NeoForge `MGT-BanItem` (target NeoForge/Forge 21.1.211).

Importante:
- Este projeto usa o formato de metadados `neoforge.mods.toml` (NeoForge 21.1.211), conforme solicitado.
- O objetivo é portar o plugin Bukkit/Spigot `BanItem` mantendo ao máximo a lógica original — eventos, módulos, comandos e configuração serão replicados quando possível.

Arquivos adicionados neste scaffold:
- `build.gradle` — build simples com dependência para `net.minecraftforge:forge:21.1.211`.
- `src/main/java/fr/andross/banitem/ModMain.java` — classe principal com `@Mod` e listener de setup.
- `src/main/resources/META-INF/neoforge.mods.toml` — metadados do mod NeoForge.
- `src/main/resources/assets/mgt_banitem/pack.mcmeta` — assets mínimos.

Regras e próximos passos da migração automática:
1. Seguir a documentação original do NeoForge 1.21.1 / 21.1.211 para empacotamento e metadados.
2. Mapear classes do plugin em `BanItemPlugin/src/main/java/fr/andross/banitem` e criar adaptadores para eventos e comandos.
3. Implementar listeners convertidos mantendo a lógica de `BanListener`, `BanHooks` e `BanItemAPI`.
4. Converter comandos para o sistema de comandos do mod (Brigadier ou compat layer) mantendo permissões e help.
5. Testar builds locais e ajustar o `build.gradle` para usar ForgeGradle / NeoForge plugin se necessário.

Status atual: scaffold criado; iniciando mapeamento de classes do plugin para conversão.

## Progresso da Migração

### ✅ Completo
- Scaffold inicial do mod (build.gradle, neoforge.mods.toml, gradle.properties, settings.gradle)
- Classe principal ModMain com inicialização NeoForge
- Sistema de configuração (BanConfig) com suporte a JSON
- Sistema de database (BanDatabase) com Blacklist e Whitelist
- Classes de ações (BanAction, BanData, BanDataType, BanActionData)
- Classe BannedItem para representar itens banidos
- BanUtils com utilitários básicos
- BanItemAPI com métodos de verificação de banimento
- BanListener com handlers básicos para eventos Forge

### 🚧 Em Progresso
- Implementação completa da lógica de verificação de blacklist/whitelist
- Conversão de todos os listeners do plugin original
- Sistema de comandos com Brigadier

### ⏳ Pendente
- Parsing completo da configuração JSON (estrutura básica implementada)
- Suporte avançado a NBT/componentes em itens banidos
- Sistema de hooks (WorldGuard, PlaceholderAPI, etc.) - planejado para versões futuras
- Implementação completa de todos os 30+ event handlers do plugin original
- Testes extensivos e ajustes finais

## Como Usar

### Build do Mod

```bash
cd MGT-BanItem
./gradlew build
```

O arquivo JAR será gerado em `build/libs/mgt_banitem-1.0.0.jar`

### Configuração

O mod cria automaticamente um arquivo de configuração em `config/mgt-banitem.json`.
Veja `mgt-banitem-example.json` para um exemplo completo de configuração.

### Comandos

- `/banitem help` ou `/bi help` - Mostra a ajuda
- `/banitem reload` ou `/bi reload` - Recarrega a configuração (requer permissão de operador)
- `/banitem info` ou `/bi info` - Mostra informações sobre itens banidos (requer permissão de operador)

### Estrutura da Configuração

```json
{
  "prefix": "&c[&eMGT-BanItem&c] ",
  "blacklist": {
    "*": {
      "item_name": {
        "action1,action2": {
          "message": "Mensagem de ban",
          "command": "comando a executar"
        }
      }
    },
    "world_name": {
      ...
    }
  },
  "whitelist": {
    ...
  }
}
```

### Ações Disponíveis

- `break` - Quebrar blocos
- `click` - Clicar com o item
- `consume` - Consumir item (comida)
- `craft` - Craftar item
- `drop` - Dropar item
- `drops` - Drops de blocos
- `hold` - Segurar item
- `interact` - Interagir com blocos
- `inventoryclick` - Clicar no inventário
- `pickup` - Pegar item do chão
- `place` - Colocar blocos
- `transfer` - Transferir entre inventários
- `use` - Usar item (botão direito)
- `wear` - Vestir armadura
- `attack` - Atacar com item
- `delete` - Deletar automaticamente

## Diferenças do Plugin Original

1. **Formato de Configuração**: Usa JSON ao invés de YAML
2. **Sistema de Eventos**: Usa eventos do NeoForge ao invés de Bukkit
3. **Comandos**: Implementado com Brigadier ao invés de comando API do Bukkit
4. **Hooks**: Sistema de hooks será implementado em versões futuras
5. **Performance**: Otimizado para servidor dedicado NeoForge

## Desenvolvimento Futuro

- [ ] Implementar todos os 30+ event handlers do plugin original
- [ ] Sistema completo de parsing de configuração com suporte a wildcards
- [ ] Suporte a componentes/NBT personalizados
- [ ] Sistema de hooks para integração com outros mods
- [ ] Interface gráfica de configuração (mod de cliente opcional)
- [ ] Metrics e estatísticas de uso
- [ ] Documentação completa da API para desenvolvedores

## Licença

GPL-3.0 - Baseado no plugin BanItem original por André Sustac
# MGT-BanItem
