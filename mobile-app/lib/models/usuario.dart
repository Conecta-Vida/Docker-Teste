class UserModel {
  final int? id;
  final String nome;
  final String email;
  final int? dataNascimento;
  final String sexo;
  final String localizacao;

  const UserModel({
    this.id,
    required this.nome,
    required this.email,
    this.dataNascimento,
    required this.sexo,
    required this.localizacao,
  });

  Map<String, dynamic> toMap({String senha = ''}) {
    return {
      'nome': nome,
      'email': email,
      'data_nascimento': dataNascimento,
      'sexo': sexo,
      'localizacao': localizacao,
      'senha': senha,
    };
  }

  factory UserModel.fromMap(Map<String, dynamic> map) {
    return UserModel(
      // O Spring pode enviar 'id' ou 'idx' dependendo da versão da entidade
      id: map['id'] ?? map['idx'],
      nome: map['nome'] ?? '',
      email: map['email'] ?? '',
      // Garante que lê 'data_nascimento' (do banco) ou 'idade' (do login)
      dataNascimento: map['data_nascimento'] is int
          ? map['data_nascimento']
          : (map['idade'] is int ? map['idade'] : null),
      sexo: map['sexo'] ?? '',
      localizacao: map['localizacao'] ?? '',
    );
  }
}
