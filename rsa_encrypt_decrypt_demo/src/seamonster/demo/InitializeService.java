/**
 * 
 */
package seamonster.demo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author Dovahkiin
 *
 */
@Service
public class InitializeService implements InitializingBean {

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		//服务初始化时生成公钥密钥对
		RSAUtils.generateKeyPair();	//生成公钥密钥对
	}

}
