# 🚀 SP_CORE - Plugin Principal do StellarPrison

## 📝 Descrição
SP_CORE é o plugin principal do servidor StellarPrison, um servidor de Minecraft com temática espacial. Ele gerencia todos os sistemas principais do servidor, desde economia até crafting espacial.

## 🌟 Funcionalidades Principais

### 💬 Sistema de Chat
- Chat Global e Local
- Formatação personalizada
- Prefixos de clãs e grupos
- Comando: `/g [mensagem]` - Alterna entre chat local e global

### 👥 Sistema de Permissões
- Gerenciamento avançado de grupos e permissões
- Sistema de prefixos customizáveis
- Herança de permissões
- Permissões temporárias
- Comandos:
  ```
  /permissoes group <nome> create <prefix>
  /permissoes group <nome> delete
  /permissoes group <nome> addperm <perm>
  /permissoes group <nome> delperm <perm>
  /permissoes group <nome> setprefix <prefix>
  /permissoes player <player> addgroup <grupo>
  /permissoes player <player> removegroup <grupo>
  ```

### 🏃 Sistema de Níveis
- Progressão de jogador
- Desbloqueio de habilidades
- Recompensas por nível
- Comando: `/level <jogador> <nível>`

### 🚀 Sistema de Naves
- Hangar personalizado por jogador
- Diferentes tipos de naves
- Sistema de combustível
- Customização de naves
- Comandos:
  ```
  /nave loja - Abre a loja de naves
  /nave info - Mostra informações da sua nave
  /nave lista - Lista todas as suas naves
  /nave combustivel - Gerencia o combustível
  /nave hangar - Abre seu hangar
  ```

### 🌍 Sistema de Planetas
- Planetas exploráveis
- Recursos únicos
- Missões planetárias
- Sistema de atmosfera

### 💨 Sistema de Oxigênio
- Gerenciamento de oxigênio no espaço
- Tanques de oxigênio
- Estações de recarga
- Sistema de alerta de baixo oxigênio

### 💰 Sistema de Economia
- Créditos Estelares (moeda do servidor)
- Sistema de banco
- Transferências entre jogadores
- Top jogadores mais ricos
- Comandos:
  ```
  /creditos enviar <jogador> <valor>
  /creditos top
  /creditos
  ```

### ⚔️ Sistema de Clãs
- Criação e gerenciamento de clãs
- Sistema de hierarquia
- Banco do clã
- Alianças e guerras
- Eventos de clã
- GUI intuitiva
- Comandos: `/clan <subcomando>`

### ⚒️ Sistema de Crafting Espacial
- Interface gráfica personalizada
- Receitas espaciais únicas
- Sistema de qualidade de itens (Comum, Raro, Épico, Lendário)
- Taxa de sucesso baseada em nível
- Comandos:
  ```
  /spacecrafting
  /sc
  /scraft
  ```

#### Itens Craftáveis
1. **Anel do Poder Estelar**
   - +10% Velocidade de Movimento
   - +5% Resistência no Espaço
   - Nível Requerido: 15

2. **Colar da Proteção Cósmica**
   - +15% Proteção Contra Radiação
   - +8% Economia de Oxigênio
   - Nível Requerido: 20

3. **Bracelete do Poder Gravitacional**
   - +12% Velocidade de Mineração
   - +10% Chance de Drop Duplo
   - Nível Requerido: 25

### 📋 Sistema de TabList
- Informações personalizadas
- Atualização em tempo real
- Exibição de estatísticas do jogador

## 🔧 Requisitos
- Servidor Minecraft 1.8+
- MongoDB
- PlaceholderAPI (opcional)

## 💾 Banco de Dados
- MongoDB para armazenamento de dados
- Configuração via config.yml

## ⚙️ Configuração
1. Instale o plugin no diretório `plugins`
2. Configure o MongoDB no `config.yml`
3. Reinicie o servidor
4. Configure as permissões desejadas

## 🔒 Permissões Principais
```yaml
sensitive.admin: Acesso a comandos administrativos
sensitive.permissions: Gerenciamento de permissões
sensitive.clearchat: Limpar chat
sensitive.gamemode: Alterar modo de jogo
sensitive.level: Gerenciar níveis
sp.creditos.admin: Comandos admin de créditos
sp.clan.admin: Comandos admin de clãs
sp_core.crafting: Usar crafting espacial
```

## 🤝 Integração
- Sistema de placeholder para outros plugins
- API para desenvolvedores
- Eventos customizados

## 📚 Comandos Administrativos
```
/sensitive reload - Recarrega o plugin
/sensitive reload <plugin> - Recarrega plugin específico
/sensitive enable <plugin> - Ativa plugin
/sensitive disable <plugin> - Desativa plugin
/sensitive load <plugin> - Carrega novo plugin
```

## 🎮 Comandos de Utilidade
```
/clearchat [all/self] - Limpa o chat
/gm <0/1/2/3> [jogador] - Altera modo de jogo
```

## 🛠️ Para Desenvolvedores
O SP_CORE fornece uma API completa para integração com outros plugins. Consulte a documentação para mais detalhes sobre eventos e métodos disponíveis.

## 📄 Licença
Todos os direitos reservados © ZeroLegion 