package br.com.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.entidades.Lancamento;
import br.com.jpautil.JPAUtil;

public class IDaoLancamentoImpl implements IDaoLancamento {

	@Override
	public List<Lancamento> consulta(Long codUser) {
		
		List<Lancamento> lista = null;
		
		// Preciso de uma forma de prover a persistência e chamar o JPAUtil
		EntityManager entityManager = JPAUtil.getEntityManager();
		// Uma transação para a operação no BD
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		lista = entityManager.createQuery("from Lancamento where usuario.id = " + codUser).getResultList();
		
		entityTransaction.commit();
		entityManager.close();

		
		return lista;
	}

}
