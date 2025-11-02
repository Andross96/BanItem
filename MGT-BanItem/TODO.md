# TODO: Implementações Futuras - MGT-BanItem

## 🎉 Recentemente Concluído

### Comandos Administrativos Completos (Nov 2024)
Implementação completa do sistema de comandos administrativos com:
- ✅ 5 novos comandos: add, remove, check, log, metaitem
- ✅ Sistema de parsing de argumentos com wildcards
- ✅ Suporte a múltiplos materiais e mundos
- ✅ Mensagens personalizadas e coloridas
- ✅ API estendida para gerenciamento de blacklist/whitelist
- ✅ Sistema de logging debug por jogador
- ✅ Gerenciamento completo de metaitems

**Arquivos criados:** `CommandHelper.java`, `CommandAdd.java`, `CommandRemove.java`, `CommandCheck.java`, `CommandLog.java`, `CommandMetaItem.java`

**Arquivos modificados:** `BanCommand.java`, `BanUtils.java`, `ModMain.java`, `BanItemAPI.java`, `BanDatabase.java`, `BannedItem.java`

---

## Prioridade Alta

### 1. Implementar Lógica de Verificação de Blacklist/Whitelist (Parcialmente Concluído)
**Arquivos**: `BanItemAPI.java`, `BanDatabase.java`

- [x] API para adicionar/remover items do blacklist (`addToBlacklist()`, `removeFromBlacklist()`)
- [x] Método `get()` no Blacklist para recuperar items banidos por mundo
- [x] Suporte a metaitems (`addMetaItem()`, `removeMetaItem()`)
- [x] Método `toItemStack()` em BannedItem
- [x] Método `reloadConfig()` em ModMain

- [ ] Implementar método `isBlacklisted()` completo
  - Verificar item no blacklist do mundo atual
  - Verificar item no blacklist global (*)
  - Suporte a wildcards de ações
  - Verificar permissões bypass do jogador

- [ ] Implementar método `isWhitelisted()` completo
  - Verificar item no whitelist do mundo atual
  - Verificar regiões (se WorldGuard disponível)
  - Suporte a exceções

- [ ] Parsing de configuração avançado
  - Suporte a `*` para todos os itens
  - Suporte a `*` para todas as ações
  - Suporte a listas de ações separadas por vírgula
  - Suporte a NBT/componentes personalizados

### 2. Completar Event Handlers
**Arquivo**: `BanListener.java`

Implementar handlers faltantes do plugin original:

- [ ] ARMORSTANDPLACE / ARMORSTANDTAKE
- [ ] BOOKEDIT (books)
- [ ] BREW (brewing stands)
- [ ] CRAFT (crafting tables)
- [ ] CRAFTER (Minecraft 1.21 crafter block)
- [ ] ENCHANT (enchanting table + anvil)
- [ ] ENTITYDROP (mob drops)
- [ ] ENTITYINTERACT (right-click entities)
- [ ] FILL / UNFILL (buckets)
- [ ] GLIDE (elytra)
- [ ] HANGINGPLACE (item frames, paintings)
- [ ] MENDING (mending enchantment)
- [ ] RENAME (anvil renaming)
- [ ] SMELT (furnaces)
- [ ] SMITH (smithing table)
- [ ] SWAP (swap hands F key)
- [ ] SWEEPINGEDGE (sweeping edge attacks)

### 3. Sistema de Permissões
**Novo arquivo**: `BanPermissions.java`

- [ ] Implementar verificação de permissões
  - `banitem.bypass.<action>` - Bypass specific action
  - `banitem.bypass.*` - Bypass all
  - `banitem.admin` - Admin commands
  - Integração com mod de permissões (se disponível)

## Prioridade Média

### 4. Comandos Administrativos Completos ✅ CONCLUÍDO
**Arquivos**: `commands/BanCommand.java`, `commands/CommandAdd.java`, `commands/CommandRemove.java`, `commands/CommandCheck.java`, `commands/CommandLog.java`, `commands/CommandMetaItem.java`, `commands/CommandHelper.java`

- [x] `/banitem add <actions> [-m materials] [-w worlds] [message]` - Adicionar item ao blacklist
- [x] `/banitem remove [-m materials] [-w worlds]` - Remover item do blacklist
- [x] `/banitem check [delete]` - Verificar itens banidos nos jogadores
- [x] `/banitem log` - Ativar modo log para debugar
- [x] `/banitem metaitem <add|remove|get|list>` - Gerenciar meta items
- [x] Sistema de parsing de argumentos com suporte a wildcards
- [x] Suporte a múltiplos materiais e mundos
- [x] Mensagens personalizadas com color codes
- [x] Aliases de comandos (/bi, /bi mi)
- [x] Verificação de permissões (operator level 2)
- [x] Helper class para parsing de ações, itens e mundos

### 5. Sistema de Mensagens Personalizadas
**Novo arquivo**: `BanMessages.java`

- [ ] Suporte a placeholders
  - %player% - Nome do jogador
  - %item% - Nome do item
  - %action% - Ação realizada
  - %world% - Mundo atual
  
- [ ] Suporte a múltiplas mensagens
- [ ] Cooldown de mensagens

### 6. Sistema de Comandos de Punição
**Arquivo**: `BanActionData.java`

- [ ] Executar comandos quando item banido é usado
- [ ] Suporte a placeholders em comandos
- [ ] Delay de execução
- [ ] Múltiplos comandos por ação

## Prioridade Baixa

### 7. Sistema de Hooks
**Novo pacote**: `hooks/`

- [ ] WorldGuard integration
  - Verificar regiões
  - Blacklist/whitelist por região
  
- [ ] PlaceholderAPI integration (se port existir)
  - Placeholders customizados em mensagens

### 8. Scanners
**Novo pacote**: `scanners/`

- [ ] WearScanner
  - Verificar armaduras equipadas periodicamente
  - Remover armaduras banidas
  
- [ ] IllegalStackScanner
  - Detectar stacks ilegais
  - Corrigir quantidade de itens

### 9. Sistema de Delete
**Arquivo**: `BanListener.java`

- [ ] Deletar itens banidos de inventários
- [ ] Verificar títulos de inventários ignorados
- [ ] Modo de segurança (backup antes de deletar)

### 10. Animações e Efeitos
**Novo arquivo**: `BanAnimation.java`

- [ ] Efeitos visuais quando item é banido
- [ ] Sons personalizados
- [ ] Partículas

### 11. Sistema de Logs
**Novo arquivo**: `BanLogger.java`

- [ ] Log de ações banidas em arquivo
- [ ] Modo debug detalhado
- [ ] Estatísticas de uso

### 12. Custom Items
**Arquivo**: `BanDatabase.java`

- [ ] Sistema de itens customizados
- [ ] Matching avançado de NBT
- [ ] Suporte a componentes do Minecraft 1.21

### 13. Meta Items
**Novo arquivo**: `items/MetaItem.java`

- [ ] Salvar itens com NBT completo
- [ ] Reutilizar em configuração
- [ ] Comandos de gerenciamento

## Otimizações

### Performance
- [ ] Cache de verificações de banimento
- [ ] Lazy loading de configuração
- [ ] Async operations quando possível

### Código
- [ ] Adicionar mais comentários JavaDoc
- [ ] Criar testes unitários
- [ ] Refatorar código duplicado
- [ ] Melhorar error handling

## Documentação

- [ ] API documentation (JavaDoc)
- [ ] Wiki com exemplos
- [ ] Tutoriais em vídeo
- [ ] FAQ

## Testes

- [ ] Testes unitários para cada classe
- [ ] Testes de integração
- [ ] Testes de performance
- [ ] Testes com diferentes versões NeoForge

## Observações de Implementação

### Diferenças Bukkit → NeoForge

1. **ItemStack vs Item**
   - Bukkit: Material enum
   - Forge: Item + ItemStack com componentes
   - Usar ItemStack.isSameItemSameComponents() para comparação completa

2. **World vs Level**
   - Bukkit: World interface
   - Forge: Level (ServerLevel para server-side)
   - Dimension identifier via level.dimension().location()

3. **Player vs ServerPlayer**
   - Bukkit: Player interface
   - Forge: ServerPlayer para lógica server-side
   - Diferentes métodos de envio de mensagens

4. **Inventory vs Container**
   - Bukkit: InventoryView, InventoryType
   - Forge: AbstractContainerMenu, MenuType
   - Sistema completamente diferente

5. **Permissions**
   - Bukkit: Permission API built-in
   - Forge: Precisa integração com mod de permissões
   - Alternativa: usar operator level (hasPermission(2))

6. **Configuration**
   - Bukkit: YamlConfiguration built-in
   - Forge: Usar Gson para JSON ou TOML4J
   - Atual: Gson implementado

7. **Scheduler**
   - Bukkit: BukkitScheduler
   - Forge: Server tick events
   - Usar TickEvent.ServerTickEvent para tarefas periódicas

8. **Events**
   - Bukkit: Event + EventHandler annotation
   - Forge: @EventBusSubscriber + @SubscribeEvent
   - Eventos são diferentes, requer mapeamento manual

### Estrutura Recomendada para Novos Features

```java
// 1. Definir enum/interface pública
public enum NovaAcao { ... }

// 2. Implementar handler de evento
@SubscribeEvent
public static void onNovoEvento(NovoEvent event) {
    // Verificar condições
    // Chamar API para verificar ban
    // Cancelar evento se necessário
}

// 3. Adicionar à API
public boolean isNovaAcaoBanned(...) {
    // Lógica de verificação
}

// 4. Adicionar comando (se necessário)
.then(Commands.literal("nova")
    .executes(context -> {
        // Lógica do comando
    }))

// 5. Documentar no README
```

---

**Mantenha este arquivo atualizado conforme implementa features!**
