package br.com.repository;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.jpautil.JPAUtil;

public class IDaoPessoaImpl implements IDaoPessoa {
	
	private EntityManager entityManager;

	@Override
	public Pessoa consultarUsuario(String login, String senha) {
		
		Pessoa pessoa = null;
		
		// Preciso de uma forma forma de prover a  persistência e chamar  o JPAUtil
		entityManager = JPAUtil.getEntityManager();

		// Uma transação para a operação no BD
		EntityTransaction entityTransaction = entityManager.getTransaction();
		//inicia a transação
		entityTransaction.begin();
		try {
		pessoa = (Pessoa) entityManager
				.createQuery("select p from Pessoa p where p.login = '" + login + "' and p.senha = '" + senha + "'")
				.getSingleResult();
		} catch (javax.persistence.NoResultException e) {// tratamento para encontrar usuário com login e senha
			// TODO: handle exception
		}
		entityTransaction.commit();
		entityManager.close();

		return pessoa;
	}

	@Override
	public List<SelectItem> listaEstados() {
		
		List<SelectItem> selectItems = new ArrayList<SelectItem>();
		//entityManager precisa ser iniciado com uma fabrica do createManager 
		entityManager = JPAUtil.getEntityManager();
		// Uma transação para a operação no BD
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
								
		//List<Estados> estados = entityManager.createQuery("from Estados").getResultList();
		List<Estados> estados = entityManager.createQuery("from Estados").getResultList();
		
		for (Estados estado : estados) {
			selectItems.add(new SelectItem(estado, estado.getNome()));
		}
				
		return selectItems;
	}

}
