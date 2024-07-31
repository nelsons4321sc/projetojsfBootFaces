package br.com.jpautil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
	
	//uma instância do hibernate ou JPA, tem que ficar disponível para toda a aplicação
		//todos os usuários tem que ter acesso criar, salvar, excluir
		// static - é única
		//essa classe é o núcleo da persistência de dados: CRUD no BD
		private static EntityManagerFactory factory = null;
		
		//recurso estático, se não existir instância do hibernate, será criada
		static {
			if(factory == null) {
			factory = Persistence.createEntityManagerFactory("projetojsf12");
			}
		}
		
		public static EntityManager getEntityManager() {
			return factory.createEntityManager();
		}
		
		
		//retornar a primary key da entidade
		public static Object getPrimaryKey(Object entity) {
			return factory.getPersistenceUnitUtil().getIdentifier(entity);
		}

}
