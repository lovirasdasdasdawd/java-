"""Five original, seamless ambient loops made with periodic DSP. No downloaded samples."""
from pathlib import Path
import numpy as np
from scipy.io import wavfile
import subprocess, tempfile, json
rate=22050;seconds=40;n=rate*seconds;u=np.arange(n)/n;freq=np.fft.rfftfreq(n,1/rate)
root=Path(__file__).resolve().parents[1];out=root/'fabric/src/main/resources/assets/donuttracker/sounds/ocean'
def noise(rng,lo,hi,tilt):
    sp=(rng.normal(size=len(freq))+1j*rng.normal(size=len(freq)))*(1-np.exp(-(freq/lo)**2))*np.exp(-(freq/hi)**2)/np.maximum(freq,20)**tilt
    sp[0]=0;sp[-1]=sp[-1].real;v=np.fft.irfft(sp,n);return v/np.std(v)
def tone(hz):return np.sin(2*np.pi*round(hz*seconds)*u)
def pulse(centre,width):return np.exp(-(((u-centre+.5)%1-.5)/width)**2)
names=['harbor_wind','river_flow','cloud_drift','lake_night','bamboo_rustle']
for index,name in enumerate(names):
    rng=np.random.default_rng(19300+index)
    low=noise(rng,45,700,.7);air=noise(rng,400,4500,.4)
    if name=='harbor_wind':
        swell=(.5+.5*np.sin(2*np.pi*4*u))**2
        signal=low*(.014+.009*swell)+air*(.005+.012*swell)
        # A faint, distant buoy with softened partials.
        for peak in [.18,.68]:signal+=(tone(392)+.19*tone(588))*.004*pulse(peak,.035)
    elif name=='river_flow':
        signal=noise(rng,90,3200,.55)*.025+air*(.006+.003*np.sin(2*np.pi*9*u))
        for i in range(17):signal+=tone(700+i*23)*.002*pulse((i*.061)%1,.008)
    elif name=='cloud_drift':
        signal=low*.009+air*.003
        for i,hz in enumerate([110,164.825,220,277.175]):signal+=tone(hz)*(.004+.002*np.sin(2*np.pi*(i+1)*u))
    elif name=='lake_night':
        signal=low*.012+air*(.003+.004*np.sin(2*np.pi*5*u)**2)
        for i in range(5):signal+=tone(2300+i*110)*.0025*pulse(.1+i*.18,.025)*(.5+.5*np.sin(2*np.pi*80*u))**6
    else:
        signal=low*.008+noise(rng,700,6200,.25)*(.006+.012*np.sin(2*np.pi*3*u)**4)
        for peak in [.12,.37,.79]:signal+=(tone(350)+tone(525)*.35)*.004*pulse(peak,.008)
    # Independent periodic left/right air layers preserve a quiet stereo space.
    stereo=np.stack([signal+noise(rng,250,3000,.5)*.002,signal+noise(rng,250,3000,.5)*.002],axis=1)
    stereo*=.034/np.sqrt(np.mean(stereo**2));peak=float(np.max(np.abs(stereo)))
    assert peak<.45 and np.isfinite(stereo).all()
    with tempfile.TemporaryDirectory() as td:
        wav=Path(td)/'loop.wav';wavfile.write(wav,rate,(stereo*32767).astype(np.int16))
        subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4','-map_metadata','-1',str(out/(name+'.ogg'))],check=True)
    print(name,'40s stereo, peak',round(peak,3))
p=out.parent.parent/'sounds.json';data=json.loads(p.read_text())
for name in names:data['ocean.'+name]={'sounds':[{'name':'donuttracker:ocean/'+name,'stream':True}]}
p.write_text(json.dumps(data,indent=2)+'\n')
