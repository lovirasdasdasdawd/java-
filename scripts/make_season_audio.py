"""Five original periodic DSP atmospheres. Requires numpy/scipy/ffmpeg; no sampled recordings."""
from pathlib import Path
import numpy as np
from scipy.io import wavfile
import subprocess,tempfile,json
rate=22050;seconds=32;n=rate*seconds;u=np.arange(n)/n;freq=np.fft.rfftfreq(n,1/rate)
root=Path(__file__).resolve().parents[1];out=root/'fabric/src/main/resources/assets/donuttracker/sounds/ocean'
def noise(rng,lo,hi,tilt):
    sp=(rng.normal(size=len(freq))+1j*rng.normal(size=len(freq)))*(1-np.exp(-(freq/lo)**2))*np.exp(-(freq/hi)**2)/np.maximum(freq,20)**tilt
    sp[0]=0;sp[-1]=sp[-1].real;v=np.fft.irfft(sp,n);return v/np.std(v)
def tone(hz):return np.sin(2*np.pi*round(hz*seconds)*u)
def bell(hz,centre,width):
    dist=(u-centre+.5)%1-.5;return (tone(hz)+.24*tone(hz*2.76)+.09*tone(hz*5.4))*np.exp(-(dist/width)**2)
names=['desert_wind','snow_hush','abyssal_drift','garden_chimes','distant_storm']
for index,name in enumerate(names):
    rng=np.random.default_rng(11100+index)
    if name=='desert_wind':
        signal=noise(rng,70,1700,.6)*(.026+.018*np.sin(2*np.pi*2*u+.4*np.sin(2*np.pi*u))**2)+noise(rng,1700,6500,.1)*.008
    elif name=='snow_hush':
        signal=noise(rng,80,1000,.8)*(.020+.006*np.cos(2*np.pi*u))+noise(rng,1500,4200,.3)*.004
        signal+=bell(1568,.23,.025)*.006+bell(1174.66,.71,.04)*.005
    elif name=='abyssal_drift':
        signal=noise(rng,35,550,.75)*.022+tone(55)*.008+tone(82.406)*(.006+.003*np.cos(2*np.pi*u))
        for hz,peak in [(392,.13),(523.25,.42),(659.25,.77)]:signal+=bell(hz,peak,.012)*.009
    elif name=='garden_chimes':
        signal=noise(rng,150,3000,.55)*(.019+.007*np.sin(2*np.pi*3*u)**2)
        for hz,peak in [(523.25,.09),(659.25,.27),(783.99,.51),(1046.5,.76),(880,.91)]:signal+=bell(hz,peak,.024)*.023
    else:
        signal=noise(rng,600,6500,.18)*.027+noise(rng,70,1200,.5)*(.015+.009*np.sin(2*np.pi*2*u)**2)
        rumble=noise(rng,28,190,.8);dist=(u-.52+.5)%1-.5;signal+=rumble*.027*np.exp(-(dist/.075)**2)
    signal*=.042/np.sqrt(np.mean(signal**2));assert np.isfinite(signal).all() and np.max(np.abs(signal))<.65
    with tempfile.TemporaryDirectory() as td:
        wav=Path(td)/'loop.wav';wavfile.write(wav,rate,(signal*32767).astype(np.int16))
        subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4','-map_metadata','-1',str(out/(name+'.ogg'))],check=True)
    print(name,'32s RMS .042 peak',round(float(np.max(np.abs(signal))),3))
p=out.parent.parent/'sounds.json';data=json.loads(p.read_text())
for name in names:data['ocean.'+name]={'sounds':[{'name':'donuttracker:ocean/'+name,'stream':True}]}
p.write_text(json.dumps(data,indent=2)+'\n')
